package com.budgetplanner.domain

import com.budgetplanner.domain.calc.RecurrenceCalculator
import com.budgetplanner.domain.model.EntryType
import com.budgetplanner.domain.model.Frequency
import com.budgetplanner.domain.model.PlanItem
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecurrenceCalculatorTest {

    private fun item(
        frequency: Frequency, start: LocalDate = longAgo, dow: DayOfWeek? = null, day: Int? = null,
        end: LocalDate? = null, active: Boolean = true,
    ) = PlanItem(title = "t", amount = 500_000, type = EntryType.EXPENSE, frequency = frequency,
        dayOfWeek = dow, dayOfMonth = day, startDate = start, endDate = end, isActive = active, categoryId = 1)

    @Test fun weeklySaturdayInSeptember2026HasFourOccurrences() {
        val dates = RecurrenceCalculator.occurrences(item(Frequency.WEEKLY, dow = DayOfWeek.SATURDAY), YearMonth.of(2026, 9))
        assertEquals((listOf(5, 12, 19, 26)).map { LocalDate.of(2026, 9, it) }, dates)
    }

    @Test fun weeklySaturdayInOctober2026HasFiveOccurrences() {
        val it = item(Frequency.WEEKLY, dow = DayOfWeek.SATURDAY)
        assertEquals(5, RecurrenceCalculator.countIn(it, YearMonth.of(2026, 10)))
        assertEquals(2_500_000, RecurrenceCalculator.totalIn(it, YearMonth.of(2026, 10)))
        assertEquals(2_000_000, RecurrenceCalculator.totalIn(it, YearMonth.of(2026, 9)))
    }

    @Test fun weeklyInFebruaryOf28DaysHasExactlyFourOccurrences() {
        // Февраль 2027 — 28 дней, каждый день недели встречается ровно 4 раза.
        DayOfWeek.entries.forEach { dow ->
            assertEquals(4, RecurrenceCalculator.countIn(item(Frequency.WEEKLY, dow = dow), YearMonth.of(2027, 2)))
        }
    }

    @Test fun weeklyInMonthOf31DaysHasThreeDaysOfWeekWithFiveOccurrences() {
        val counts = DayOfWeek.entries.map {
            RecurrenceCalculator.countIn(item(Frequency.WEEKLY, dow = it), YearMonth.of(2026, 10))
        }
        assertEquals(3, counts.count { it == 5 })
        assertEquals(31, counts.sum())
    }

    @Test fun weeklyInMonthOf30DaysCoversAllDays() {
        val counts = DayOfWeek.entries.map {
            RecurrenceCalculator.countIn(item(Frequency.WEEKLY, dow = it), YearMonth.of(2026, 9))
        }
        assertEquals(30, counts.sum())
    }

    @Test fun monthlyDay5InMonthsOf28_30_31Days() {
        val it = item(Frequency.MONTHLY, day = 5)
        listOf(YearMonth.of(2027, 2), YearMonth.of(2026, 4), YearMonth.of(2026, 10)).forEach { m ->
            assertEquals(listOf(m.atDay(5)), RecurrenceCalculator.occurrences(it, m))
        }
    }

    @Test fun monthlyDay31ClampsToLastDayOfShortMonths() {
        val it = item(Frequency.MONTHLY, day = 31)
        assertEquals(LocalDate.of(2027, 2, 28), RecurrenceCalculator.occurrences(it, YearMonth.of(2027, 2)).single())
        assertEquals(LocalDate.of(2028, 2, 29), RecurrenceCalculator.occurrences(it, YearMonth.of(2028, 2)).single())
        assertEquals(LocalDate.of(2026, 4, 30), RecurrenceCalculator.occurrences(it, YearMonth.of(2026, 4)).single())
        assertEquals(LocalDate.of(2026, 10, 31), RecurrenceCalculator.occurrences(it, YearMonth.of(2026, 10)).single())
    }

    @Test fun lastDayOfMonthWorksForEveryMonthLength() {
        val it = lastDayExpense(1)
        assertEquals(LocalDate.of(2027, 2, 28), RecurrenceCalculator.occurrences(it, YearMonth.of(2027, 2)).single())
        assertEquals(LocalDate.of(2028, 2, 29), RecurrenceCalculator.occurrences(it, YearMonth.of(2028, 2)).single())
        assertEquals(LocalDate.of(2026, 9, 30), RecurrenceCalculator.occurrences(it, YearMonth.of(2026, 9)).single())
        assertEquals(LocalDate.of(2026, 12, 31), RecurrenceCalculator.occurrences(it, YearMonth.of(2026, 12)).single())
    }

    @Test fun onceOnlyInItsOwnMonth() {
        val it = item(Frequency.ONCE, start = LocalDate.of(2026, 10, 15))
        assertEquals(listOf(LocalDate.of(2026, 10, 15)), RecurrenceCalculator.occurrences(it, YearMonth.of(2026, 10)))
        assertTrue(RecurrenceCalculator.occurrences(it, YearMonth.of(2026, 9)).isEmpty())
        assertTrue(RecurrenceCalculator.occurrences(it, YearMonth.of(2026, 11)).isEmpty())
    }

    @Test fun dailyGivesOneOccurrencePerDayAndRespectsStartDate() {
        assertEquals(30, RecurrenceCalculator.countIn(item(Frequency.DAILY), YearMonth.of(2026, 9)))
        assertEquals(29, RecurrenceCalculator.countIn(item(Frequency.DAILY), YearMonth.of(2028, 2)))
        val fromMid = item(Frequency.DAILY, start = LocalDate.of(2026, 9, 21))
        assertEquals(10, RecurrenceCalculator.countIn(fromMid, YearMonth.of(2026, 9)))
    }

    @Test fun yearlyOccursOnlyInItsMonthAndNotBeforeStart() {
        val it = item(Frequency.YEARLY, start = LocalDate.of(2025, 3, 10))
        assertEquals(listOf(LocalDate.of(2026, 3, 10)), RecurrenceCalculator.occurrences(it, YearMonth.of(2026, 3)))
        assertTrue(RecurrenceCalculator.occurrences(it, YearMonth.of(2026, 4)).isEmpty())
        assertTrue(RecurrenceCalculator.occurrences(it, YearMonth.of(2024, 3)).isEmpty())
    }

    @Test fun yearlyFeb29FallsBackToFeb28InNonLeapYear() {
        val it = item(Frequency.YEARLY, start = LocalDate.of(2024, 2, 29))
        assertEquals(LocalDate.of(2027, 2, 28), RecurrenceCalculator.occurrences(it, YearMonth.of(2027, 2)).single())
        assertEquals(LocalDate.of(2028, 2, 29), RecurrenceCalculator.occurrences(it, YearMonth.of(2028, 2)).single())
    }

    @Test fun inactiveItemHasNoOccurrences() {
        assertTrue(RecurrenceCalculator.occurrences(item(Frequency.MONTHLY, day = 5, active = false), YearMonth.of(2026, 9)).isEmpty())
    }

    @Test fun startDateInFutureMonthMeansNothingBeforeIt() {
        val it = item(Frequency.MONTHLY, day = 5, start = LocalDate.of(2026, 11, 1))
        assertTrue(RecurrenceCalculator.occurrences(it, YearMonth.of(2026, 10)).isEmpty())
        assertEquals(1, RecurrenceCalculator.countIn(it, YearMonth.of(2026, 11)))
    }

    @Test fun startDateAfterDayInSameMonthSkipsThatMonth() {
        val it = item(Frequency.MONTHLY, day = 5, start = LocalDate.of(2026, 9, 10))
        assertTrue(RecurrenceCalculator.occurrences(it, YearMonth.of(2026, 9)).isEmpty())
        assertEquals(1, RecurrenceCalculator.countIn(it, YearMonth.of(2026, 10)))
    }

    @Test fun endDateStopsOccurrences() {
        val it = item(Frequency.WEEKLY, dow = DayOfWeek.SATURDAY, end = LocalDate.of(2026, 9, 15))
        assertEquals(listOf(5, 12), RecurrenceCalculator.occurrences(it, YearMonth.of(2026, 9)).map { d -> d.dayOfMonth })
        assertTrue(RecurrenceCalculator.occurrences(it, YearMonth.of(2026, 10)).isEmpty())
    }
}
