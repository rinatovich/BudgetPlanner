package com.budgetplanner.domain.calc

import com.budgetplanner.domain.model.Category
import com.budgetplanner.domain.model.EntryType
import com.budgetplanner.domain.model.PlanItem
import com.budgetplanner.domain.model.Transaction
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.roundToLong

/**
 * Считает бюджет месяца.
 *
 * Модель «конвертов»: план по категории — это лимит на месяц. Факт внутри лимита
 * прогноз не меняет (деньги и так были запланированы). Факт сверх лимита, а также
 * факт в категориях без плана увеличивает прогноз. Итого по категории = max(план, факт).
 * Так плановая зарплата и фактическое поступление зарплаты не считаются дважды.
 */
object BudgetCalculator {

    fun summarize(
        month: YearMonth,
        today: LocalDate,
        inputs: BudgetInputs,
    ): MonthSummary = summarize(month, today, inputs.planItems, inputs.transactions, inputs.categories)

    fun summarize(
        month: YearMonth,
        today: LocalDate,
        planItems: List<PlanItem>,
        transactions: List<Transaction>,
        categories: List<Category>,
    ): MonthSummary {
        val categoryById = categories.associateBy { it.id }
        val monthTransactions = transactions.filter { YearMonth.from(it.date) == month }

        val plannedEvents = planItems.flatMap { item ->
            RecurrenceCalculator.occurrences(item, month).map { date ->
                CashEvent(
                    date = date,
                    amount = item.amount,
                    isIncome = item.type == EntryType.INCOME,
                    categoryId = item.categoryId,
                    isEssential = item.type == EntryType.EXPENSE && item.isEssential,
                    title = item.title,
                    isPlanned = true,
                )
            }
        }

        val excessEvents = excessEvents(plannedEvents, monthTransactions, categoryById)
        val events = (plannedEvents + excessEvents).sortedBy { it.date }

        val income = events.filter { it.isIncome }.sumOf { it.amount }
        val expenseEvents = events.filter { !it.isIncome }
        val expenses = expenseEvents.sumOf { it.amount }
        val essential = expenseEvents.filter { it.isEssential }.sumOf { it.amount }

        val free = income - expenses
        val remainingDays = remainingDays(month, today)
        val daily = dailyBalance(month, events)

        return MonthSummary(
            month = month,
            income = income,
            expenses = expenses,
            essentialExpenses = essential,
            nonEssentialExpenses = expenses - essential,
            free = free,
            plannedIncome = plannedEvents.filter { it.isIncome }.sumOf { it.amount },
            plannedExpenses = plannedEvents.filter { !it.isIncome }.sumOf { it.amount },
            actualIncome = monthTransactions.filter { it.type == EntryType.INCOME }.sumOf { it.amount },
            actualExpenses = monthTransactions.filter { it.type == EntryType.EXPENSE }.sumOf { it.amount },
            remainingDays = remainingDays,
            weeklyFree = weeklyFree(free, remainingDays),
            expenseCategories = categoryTotals(events, plannedEvents, monthTransactions, EntryType.EXPENSE),
            incomeCategories = categoryTotals(events, plannedEvents, monthTransactions, EntryType.INCOME),
            dailyBalance = daily,
            lowestPoint = daily.minByOrNull { it.balance },
            essentialShareOfIncomePercent =
                if (income > 0) (essential * 100.0 / income).roundToLong().toInt() else null,
            hasData = events.isNotEmpty(),
        )
    }

    /** Сколько дней осталось (включая сегодня). Будущий месяц — весь месяц, прошлый — 0. */
    fun remainingDays(month: YearMonth, today: LocalDate): Int {
        val first = month.atDay(1)
        val last = month.atEndOfMonth()
        return when {
            today.isAfter(last) -> 0
            today.isBefore(first) -> month.lengthOfMonth()
            else -> (ChronoUnit.DAYS.between(today, last) + 1).toInt()
        }
    }

    /** Свободные деньги, поделённые на оставшиеся недели (не меньше одной недели). */
    fun weeklyFree(free: Long, remainingDays: Int): Long? {
        if (remainingDays <= 0) return null
        val positive = max(free, 0L)
        val weeks = max(1.0, remainingDays / 7.0)
        return (positive / weeks).roundToLong()
    }

    private fun excessEvents(
        planned: List<CashEvent>,
        monthTransactions: List<Transaction>,
        categoryById: Map<Long, Category>,
    ): List<CashEvent> {
        val plannedByKey = planned.groupBy { it.isIncome to it.categoryId }
        val result = mutableListOf<CashEvent>()

        monthTransactions
            .groupBy { (it.type == EntryType.INCOME) to it.categoryId }
            .forEach { (key, list) ->
                val (isIncome, categoryId) = key
                val plannedInCategory = plannedByKey[key].orEmpty()
                val plannedTotal = plannedInCategory.sumOf { it.amount }
                val essential = !isIncome && isEssential(plannedInCategory, categoryById[categoryId])

                var before = 0L
                list.sortedWith(compareBy({ it.date }, { it.id })).forEach { t ->
                    val after = before + t.amount
                    val excess = max(0L, after - plannedTotal) - max(0L, before - plannedTotal)
                    if (excess > 0) {
                        result += CashEvent(t.date, excess, isIncome, categoryId, essential, t.title, false)
                    }
                    before = after
                }
            }
        return result
    }

    private fun isEssential(plannedInCategory: List<CashEvent>, category: Category?): Boolean = when {
        plannedInCategory.isEmpty() -> category?.isEssentialDefault ?: false
        else -> plannedInCategory.any { it.isEssential }
    }

    private fun categoryTotals(
        events: List<CashEvent>,
        planned: List<CashEvent>,
        transactions: List<Transaction>,
        type: EntryType,
    ): List<CategoryTotal> {
        val isIncome = type == EntryType.INCOME
        val typeEvents = events.filter { it.isIncome == isIncome }
        return typeEvents.groupBy { it.categoryId }
            .map { (categoryId, list) ->
                CategoryTotal(
                    categoryId = categoryId,
                    planned = planned.filter { it.isIncome == isIncome && it.categoryId == categoryId }.sumOf { it.amount },
                    actual = transactions.filter { it.type == type && it.categoryId == categoryId }.sumOf { it.amount },
                    projected = list.sumOf { it.amount },
                    isEssential = list.any { it.isEssential },
                )
            }
            .sortedByDescending { it.projected }
    }

    /** Накопленное изменение баланса по дням месяца. */
    private fun dailyBalance(month: YearMonth, events: List<CashEvent>): List<DailyBalance> {
        val byDay = events.groupBy { it.date.dayOfMonth }
        var balance = 0L
        return (1..month.lengthOfMonth()).map { day ->
            balance += byDay[day].orEmpty().sumOf { it.signed }
            DailyBalance(day, balance)
        }
    }
}
