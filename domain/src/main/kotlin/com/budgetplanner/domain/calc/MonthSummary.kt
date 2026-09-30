package com.budgetplanner.domain.calc

import com.budgetplanner.domain.model.Category
import com.budgetplanner.domain.model.PlanItem
import com.budgetplanner.domain.model.Transaction
import java.time.LocalDate
import java.time.YearMonth

/** Все данные, из которых считается бюджет. */
data class BudgetInputs(
    val planItems: List<PlanItem>,
    val transactions: List<Transaction>,
    val categories: List<Category>,
)

/** Денежное событие месяца: плановое или «сверх плана» (факт, превысивший план категории). */
data class CashEvent(
    val date: LocalDate,
    val amount: Long,
    val isIncome: Boolean,
    val categoryId: Long,
    val isEssential: Boolean,
    val title: String,
    val isPlanned: Boolean,
) {
    val signed: Long get() = if (isIncome) amount else -amount
}

data class CategoryTotal(
    val categoryId: Long,
    /** Сумма по плану на месяц. */
    val planned: Long,
    /** Фактически потрачено/получено. */
    val actual: Long,
    /** Итог по прогнозу: max(план, факт). */
    val projected: Long,
    val isEssential: Boolean,
) {
    val leftInPlan: Long get() = (planned - actual).coerceAtLeast(0)
}

data class DailyBalance(val day: Int, val balance: Long)

data class MonthSummary(
    val month: YearMonth,
    val income: Long,
    val expenses: Long,
    val essentialExpenses: Long,
    val nonEssentialExpenses: Long,
    /** Прогноз остатка к концу месяца: доходы − расходы. */
    val free: Long,
    val plannedIncome: Long,
    val plannedExpenses: Long,
    val actualIncome: Long,
    val actualExpenses: Long,
    /** Сколько дней осталось в месяце (включая сегодня); 0 для прошедших месяцев. */
    val remainingDays: Int,
    /** Свободно в неделю на оставшийся период; null, если месяц уже прошёл. */
    val weeklyFree: Long?,
    val expenseCategories: List<CategoryTotal>,
    val incomeCategories: List<CategoryTotal>,
    val dailyBalance: List<DailyBalance>,
    val lowestPoint: DailyBalance?,
    val essentialShareOfIncomePercent: Int?,
    val hasData: Boolean,
) {
    val isOverspent: Boolean get() = free < 0
}
