package com.budgetplanner.domain.calc

import com.budgetplanner.domain.model.Frequency
import com.budgetplanner.domain.model.LAST_DAY_OF_MONTH
import com.budgetplanner.domain.model.PlanItem
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

/**
 * Календарный расчёт: возвращает реальные даты, на которые приходится плановая операция
 * в конкретном месяце. Никаких «×4»: в месяце может быть 4 или 5 суббот.
 */
object RecurrenceCalculator {

    fun occurrences(item: PlanItem, month: YearMonth): List<LocalDate> {
        if (!item.isActive) return emptyList()

        val from = maxOf(month.atDay(1), item.startDate)
        val monthEnd = month.atEndOfMonth()
        val to = item.endDate?.let { minOf(monthEnd, it) } ?: monthEnd
        if (from.isAfter(to)) return emptyList()

        val dates: List<LocalDate> = when (item.frequency) {
            Frequency.ONCE -> listOf(item.startDate)
            Frequency.DAILY -> generateSequence(from) { it.plusDays(1) }
                .takeWhile { !it.isAfter(to) }
                .toList()
            Frequency.WEEKLY -> weekly(item, from, to)
            Frequency.MONTHLY -> {
                val day = item.dayOfMonth ?: item.startDate.dayOfMonth
                listOf(month.atDay(resolveDayOfMonth(day, month)))
            }
            Frequency.YEARLY ->
                if (month.monthValue == item.startDate.monthValue) {
                    listOf(month.atDay(resolveDayOfMonth(item.startDate.dayOfMonth, month)))
                } else {
                    emptyList()
                }
        }
        return dates.filter { !it.isBefore(from) && !it.isAfter(to) }
    }

    fun countIn(item: PlanItem, month: YearMonth): Int = occurrences(item, month).size

    /** Сумма операции за месяц с учётом реального числа повторений. */
    fun totalIn(item: PlanItem, month: YearMonth): Long = item.amount * countIn(item, month)

    /** День 31 в феврале → 28/29; [LAST_DAY_OF_MONTH] → последний день. */
    fun resolveDayOfMonth(day: Int, month: YearMonth): Int {
        val length = month.lengthOfMonth()
        return if (day == LAST_DAY_OF_MONTH) length else day.coerceIn(1, length)
    }

    private fun weekly(item: PlanItem, from: LocalDate, to: LocalDate): List<LocalDate> {
        val dow = item.dayOfWeek ?: item.startDate.dayOfWeek
        val first = from.with(TemporalAdjusters.nextOrSame(dow))
        return generateSequence(first) { it.plusWeeks(1) }
            .takeWhile { !it.isAfter(to) }
            .toList()
    }
}
