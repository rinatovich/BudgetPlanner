package com.budgetplanner.domain

import com.budgetplanner.domain.calc.BudgetCalculator
import com.budgetplanner.domain.calc.BudgetInputs
import com.budgetplanner.domain.calc.MonthSummary
import com.budgetplanner.domain.calc.ScenarioSimulator
import com.budgetplanner.domain.model.EntryType
import com.budgetplanner.domain.model.Frequency
import com.budgetplanner.domain.model.PlanItem
import com.budgetplanner.domain.model.Transaction
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BudgetCalculatorTest {

    private val sep = YearMonth.of(2026, 9)
    private val sep1 = LocalDate.of(2026, 9, 1)

    private fun summary(
        plan: List<PlanItem> = emptyList(),
        tx: List<Transaction> = emptyList(),
        month: YearMonth = sep,
        today: LocalDate = sep1,
    ): MonthSummary = BudgetCalculator.summarize(month, today, plan, tx, testCategories)

    // ---- Сценарий приёмки из ТЗ (раздел 43) ----

    @Test fun acceptanceScenarioFromSpec() {
        val s = summary(
            plan = listOf(
                monthlyIncome(40_000_000, 5),
                weeklyExpense(500_000, DayOfWeek.SATURDAY),
                monthlyExpense(8_000_000, 10, category = Ids.CREDIT, title = "Кредит"),
            ),
        )
        assertEquals(40_000_000, s.income)
        assertEquals(10_000_000, s.expenses) // 8M + 4 субботы * 500K
        assertEquals(30_000_000, s.free)
        assertEquals(10_000_000, s.essentialExpenses)
        assertEquals(0, s.nonEssentialExpenses)
        assertEquals(25, s.essentialShareOfIncomePercent)
        assertEquals(30, s.remainingDays)
        assertEquals(7_000_000L, s.weeklyFree) // 30M на 30/7 недели
    }

    @Test fun actualExpenseFromSpecStep7DoesNotBreakForecast() {
        val plan = listOf(monthlyIncome(40_000_000, 5), weeklyExpense(500_000, DayOfWeek.SATURDAY))
        val before = summary(plan)
        val after = summary(plan, listOf(expenseTx(180_000, LocalDate.of(2026, 9, 1))))
        // 180K внутри плановых 2M на продукты — прогноз не меняется, но факт виден.
        assertEquals(before.free, after.free)
        assertEquals(180_000, after.actualExpenses)
        assertEquals(1_820_000, after.expenseCategories.single().leftInPlan)
    }

    // ---- Длина месяца ----

    @Test fun weeklyExpenseDependsOnActualCountOfWeekdays() {
        val plan = listOf(weeklyExpense(500_000, DayOfWeek.SATURDAY))
        assertEquals(2_000_000, summary(plan, month = sep).expenses)                        // 30 дней, 4 субботы
        assertEquals(2_500_000, summary(plan, month = YearMonth.of(2026, 10)).expenses)     // 31 день, 5 суббот
        assertEquals(2_000_000, summary(plan, month = YearMonth.of(2027, 2)).expenses)      // 28 дней, 4 субботы
    }

    @Test fun expenseOnLastDayOfMonthLandsOnLastDayInDailyBalance() {
        val feb = YearMonth.of(2027, 2)
        val s = summary(listOf(monthlyIncome(10_000_000, 1), lastDayExpense(4_000_000)), month = feb, today = LocalDate.of(2027, 2, 1))
        assertEquals(28, s.dailyBalance.size)
        assertEquals(10_000_000, s.dailyBalance[26].balance) // 27 февраля
        assertEquals(6_000_000, s.dailyBalance[27].balance)  // 28 февраля — списание
    }

    @Test fun monthWith31DaysHas31DailyPoints() {
        assertEquals(31, summary(month = YearMonth.of(2026, 10)).dailyBalance.size)
    }

    // ---- Доходы ----

    @Test fun multipleIncomesAreSummed() {
        val s = summary(listOf(monthlyIncome(40_000_000, 5), monthlyIncome(2_000_000, 20, category = Ids.SIDE, title = "Подработка")))
        assertEquals(42_000_000, s.income)
        assertEquals(2, s.incomeCategories.size)
    }

    @Test fun noIncomeGivesNegativeFreeAndNoShare() {
        val s = summary(listOf(monthlyExpense(3_000_000, 1)))
        assertEquals(0, s.income)
        assertEquals(-3_000_000, s.free)
        assertNull(s.essentialShareOfIncomePercent)
        assertTrue(s.isOverspent)
        assertEquals(0L, s.weeklyFree)
    }

    @Test fun emptyMonthHasNoData() {
        val s = summary()
        assertFalse(s.hasData)
        assertEquals(0, s.free)
        assertFalse(s.isOverspent)
    }

    // ---- Остаток ----

    @Test fun expensesExceedIncome() {
        val s = summary(listOf(monthlyIncome(10_000_000, 5), monthlyExpense(12_500_000, 1)))
        assertEquals(-2_500_000, s.free)
        assertTrue(s.isOverspent)
        assertEquals(125, s.essentialShareOfIncomePercent)
    }

    @Test fun zeroBalance() {
        val s = summary(listOf(monthlyIncome(10_000_000, 5), monthlyExpense(10_000_000, 6)))
        assertEquals(0, s.free)
        assertFalse(s.isOverspent)
        assertEquals(0L, s.weeklyFree)
    }

    @Test fun essentialAndNonEssentialAreSeparated() {
        val s = summary(listOf(
            monthlyIncome(45_000_000, 5),
            monthlyExpense(27_000_000, 1, essential = true),
            monthlyExpense(6_000_000, 2, essential = false, category = Ids.FUN),
        ))
        assertEquals(27_000_000, s.essentialExpenses)
        assertEquals(6_000_000, s.nonEssentialExpenses)
        assertEquals(12_000_000, s.free)
        assertEquals(60, s.essentialShareOfIncomePercent)
    }

    // ---- Свободно в неделю ----

    @Test fun weeklyFreeDependsOnRemainingDays() {
        val plan = listOf(monthlyIncome(12_000_000, 1))
        assertEquals(3_000_000L, summary(plan, today = LocalDate.of(2026, 9, 3)).weeklyFree)  // 28 дней = 4 недели
        assertEquals(12_000_000L, summary(plan, today = LocalDate.of(2026, 9, 30)).weeklyFree) // 1 день → не меньше недели
    }

    @Test fun weeklyFreeForFutureAndPastMonths() {
        val plan = listOf(monthlyIncome(12_000_000, 1))
        val future = summary(plan, month = YearMonth.of(2026, 10), today = LocalDate.of(2026, 9, 15))
        assertEquals(31, future.remainingDays)
        assertNotNull(future.weeklyFree)
        val past = summary(plan, month = YearMonth.of(2026, 8), today = LocalDate.of(2026, 9, 15))
        assertEquals(0, past.remainingDays)
        assertNull(past.weeklyFree)
    }

    // ---- Факт и план (модель «конвертов») ----

    @Test fun actualIncomeMatchingPlannedSalaryIsNotCountedTwice() {
        val plan = listOf(monthlyIncome(40_000_000, 5))
        val s = summary(plan, listOf(incomeTx(40_000_000, LocalDate.of(2026, 9, 5))))
        assertEquals(40_000_000, s.income)
        assertEquals(40_000_000, s.actualIncome)
    }

    @Test fun actualAbovePlanIncreasesForecast() {
        val plan = listOf(monthlyIncome(10_000_000, 1), monthlyExpense(3_000_000, 1, category = Ids.GROCERIES))
        val s = summary(plan, listOf(expenseTx(3_500_000, LocalDate.of(2026, 9, 20))))
        assertEquals(3_500_000, s.expenses)
        assertEquals(6_500_000, s.free)
        assertEquals(3_500_000, s.expenseCategories.single().projected)
        assertEquals(0, s.expenseCategories.single().leftInPlan)
    }

    @Test fun excessIsPlacedOnTheDayThePlanWasExceeded() {
        val plan = listOf(monthlyExpense(3_000_000, 1, category = Ids.GROCERIES))
        val s = summary(plan, listOf(
            expenseTx(2_000_000, LocalDate.of(2026, 9, 3), id = 1),
            expenseTx(2_000_000, LocalDate.of(2026, 9, 10), id = 2),
        ))
        assertEquals(4_000_000, s.expenses)
        assertEquals(-3_000_000, s.dailyBalance[8].balance)   // 9 сентября: только план
        assertEquals(-4_000_000, s.dailyBalance[9].balance)   // 10 сентября: +1M сверх плана
    }

    @Test fun unplannedActualExpenseCountsAndUsesCategoryEssentialFlag() {
        val s = summary(
            plan = listOf(monthlyIncome(10_000_000, 1)),
            tx = listOf(expenseTx(100_000, LocalDate.of(2026, 9, 2), Ids.FUN), expenseTx(200_000, LocalDate.of(2026, 9, 2), Ids.GROCERIES)),
        )
        assertEquals(300_000, s.expenses)
        assertEquals(200_000, s.essentialExpenses)
        assertEquals(100_000, s.nonEssentialExpenses)
        assertEquals(9_700_000, s.free)
    }

    @Test fun transactionsOfOtherMonthsAreIgnored() {
        val s = summary(tx = listOf(expenseTx(500_000, LocalDate.of(2026, 8, 31)), expenseTx(500_000, LocalDate.of(2026, 10, 1))))
        assertEquals(0, s.expenses)
        assertFalse(s.hasData)
    }

    // ---- График по дням ----

    @Test fun dailyBalanceEndsAtFreeAndFindsLowestPoint() {
        val s = summary(listOf(
            monthlyExpense(5_000_000, 1),
            monthlyIncome(40_000_000, 5),
            monthlyExpense(8_000_000, 10, category = Ids.CREDIT),
        ))
        assertEquals(s.free, s.dailyBalance.last().balance)
        assertEquals(1, s.lowestPoint?.day)
        assertEquals(-5_000_000, s.lowestPoint?.balance)
    }

    @Test fun categoriesSortedByProjectedDescending() {
        val s = summary(listOf(
            monthlyExpense(1_000_000, 1, category = Ids.FUN, essential = false),
            monthlyExpense(8_000_000, 10, category = Ids.CREDIT),
            monthlyExpense(5_000_000, 1, category = Ids.HOUSING),
        ))
        assertEquals(listOf(Ids.CREDIT, Ids.HOUSING, Ids.FUN), s.expenseCategories.map { it.categoryId })
    }

    // ---- Изменение и удаление регулярных расходов ----

    @Test fun editingRecurringExpenseChangesForecast() {
        val original = monthlyExpense(5_000_000, 1, id = 7)
        val plan = listOf(monthlyIncome(20_000_000, 5), original)
        assertEquals(15_000_000, summary(plan).free)
        val edited = plan.map { if (it.id == 7L) it.copy(amount = 6_000_000) else it }
        assertEquals(14_000_000, summary(edited).free)
        val switchedToWeekly = plan.map { if (it.id == 7L) it.copy(frequency = Frequency.WEEKLY, dayOfWeek = DayOfWeek.FRIDAY) else it }
        assertEquals(20_000_000 - 4 * 5_000_000, summary(switchedToWeekly).free) // 4 пятницы в сентябре 2026
    }

    @Test fun deletingOrDisablingRecurringExpenseRestoresForecast() {
        val plan = listOf(monthlyIncome(20_000_000, 5), monthlyExpense(5_000_000, 1, id = 7))
        assertEquals(20_000_000, summary(plan.filter { it.id != 7L }).free)
        assertEquals(20_000_000, summary(plan.map { if (it.id == 7L) it.copy(isActive = false) else it }).free)
    }

    // ---- Сценарий «что если» ----

    @Test fun scenarioShowsImpactOfNewOneTimeExpense() {
        val inputs = BudgetInputs(listOf(monthlyIncome(14_000_000, 5), monthlyExpense(2_000_000, 1, id = 3)), emptyList(), testCategories)
        val phone = PlanItem(title = "Новый телефон", amount = 2_000_000, type = EntryType.EXPENSE, frequency = Frequency.ONCE,
            startDate = LocalDate.of(2026, 9, 20), categoryId = Ids.FUN)
        val r = ScenarioSimulator.simulate(sep, sep1, inputs, phone)
        assertEquals(12_000_000, r.freeBefore)
        assertEquals(10_000_000, r.freeAfter)
        assertEquals(-2_000_000, r.delta)
    }

    @Test fun scenarioForExistingItemReplacesItInsteadOfAdding() {
        val existing = monthlyExpense(2_000_000, 1, id = 3)
        val inputs = BudgetInputs(listOf(monthlyIncome(14_000_000, 5), existing), emptyList(), testCategories)
        val r = ScenarioSimulator.simulate(sep, sep1, inputs, existing.copy(amount = 3_000_000))
        assertEquals(12_000_000, r.freeBefore)
        assertEquals(11_000_000, r.freeAfter)
    }

    // ---- Неделя без ошибок округления ----

    @Test fun remainingDaysHelper() {
        assertEquals(30, BudgetCalculator.remainingDays(sep, LocalDate.of(2026, 9, 1)))
        assertEquals(1, BudgetCalculator.remainingDays(sep, LocalDate.of(2026, 9, 30)))
        assertEquals(0, BudgetCalculator.remainingDays(sep, LocalDate.of(2026, 10, 1)))
        assertEquals(30, BudgetCalculator.remainingDays(sep, LocalDate.of(2026, 8, 20)))
    }
}
