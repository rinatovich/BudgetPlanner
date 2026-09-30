package com.budgetplanner.app.ui.components

import com.budgetplanner.domain.model.Frequency
import com.budgetplanner.domain.model.LAST_DAY_OF_MONTH
import com.budgetplanner.domain.model.PlanItem
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val RU: Locale = Locale.forLanguageTag("ru")

private fun String.capitalizeRu(): String = replaceFirstChar { it.uppercase(RU) }

/** «Сентябрь 2026» */
fun monthTitle(month: YearMonth): String =
    month.atDay(1).format(DateTimeFormatter.ofPattern("LLLL yyyy", RU)).capitalizeRu()

/** «Сегодня», «Вчера», «15 сентября» */
fun dayLabel(date: LocalDate, today: LocalDate = LocalDate.now()): String = when (date) {
    today -> "Сегодня"
    today.minusDays(1) -> "Вчера"
    else -> {
        val base = date.format(DateTimeFormatter.ofPattern("d MMMM", RU))
        if (date.year != today.year) "$base ${date.year}" else base
    }
}

/** «15 октября 2026» */
fun fullDate(date: LocalDate): String = date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", RU))

fun weekdayShort(day: DayOfWeek): String = when (day) {
    DayOfWeek.MONDAY -> "Пн"
    DayOfWeek.TUESDAY -> "Вт"
    DayOfWeek.WEDNESDAY -> "Ср"
    DayOfWeek.THURSDAY -> "Чт"
    DayOfWeek.FRIDAY -> "Пт"
    DayOfWeek.SATURDAY -> "Сб"
    DayOfWeek.SUNDAY -> "Вс"
}

private fun weekdayEvery(day: DayOfWeek): String = when (day) {
    DayOfWeek.MONDAY -> "каждый понедельник"
    DayOfWeek.TUESDAY -> "каждый вторник"
    DayOfWeek.WEDNESDAY -> "каждую среду"
    DayOfWeek.THURSDAY -> "каждый четверг"
    DayOfWeek.FRIDAY -> "каждую пятницу"
    DayOfWeek.SATURDAY -> "каждую субботу"
    DayOfWeek.SUNDAY -> "каждое воскресенье"
}

fun frequencyLabel(frequency: Frequency): String = when (frequency) {
    Frequency.ONCE -> "Единоразово"
    Frequency.DAILY -> "Ежедневно"
    Frequency.WEEKLY -> "Еженедельно"
    Frequency.MONTHLY -> "Ежемесячно"
    Frequency.YEARLY -> "Ежегодно"
}

/** «/ месяц», «/ неделя» … */
fun frequencyUnit(frequency: Frequency): String = when (frequency) {
    Frequency.ONCE -> ""
    Frequency.DAILY -> "/ день"
    Frequency.WEEKLY -> "/ неделя"
    Frequency.MONTHLY -> "/ месяц"
    Frequency.YEARLY -> "/ год"
}

/** «5 числа», «каждую субботу», «последний день месяца» */
fun scheduleText(item: PlanItem): String = when (item.frequency) {
    Frequency.ONCE -> fullDate(item.startDate)
    Frequency.DAILY -> "каждый день"
    Frequency.WEEKLY -> weekdayEvery(item.dayOfWeek ?: item.startDate.dayOfWeek)
    Frequency.MONTHLY -> {
        val day = item.dayOfMonth ?: item.startDate.dayOfMonth
        if (day == LAST_DAY_OF_MONTH) "последний день месяца" else "$day числа"
    }
    Frequency.YEARLY -> "каждый год, " + item.startDate.format(DateTimeFormatter.ofPattern("d MMMM", RU))
}

/** Дни недели в порядке, начиная с выбранного первого дня. */
fun orderedWeekdays(first: DayOfWeek): List<DayOfWeek> =
    (0 until 7).map { first.plus(it.toLong()) }
