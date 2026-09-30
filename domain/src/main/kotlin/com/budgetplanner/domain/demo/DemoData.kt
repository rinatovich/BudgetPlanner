package com.budgetplanner.domain.demo

import com.budgetplanner.domain.model.EntryType
import com.budgetplanner.domain.model.Frequency
import com.budgetplanner.domain.model.LAST_DAY_OF_MONTH
import com.budgetplanner.domain.model.PlanItem
import com.budgetplanner.domain.model.Transaction
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/** Демо-данные для быстрой проверки графиков и UX. */
object DemoData {

    fun planItems(today: LocalDate, categoryId: (name: String, type: EntryType) -> Long): List<PlanItem> {
        val start = YearMonth.from(today).minusMonths(3).atDay(1)
        fun income(title: String, cat: String, amount: Long, day: Int) = PlanItem(
            title = title, amount = amount, type = EntryType.INCOME, frequency = Frequency.MONTHLY,
            dayOfMonth = day, startDate = start, categoryId = categoryId(cat, EntryType.INCOME),
        )
        fun expense(
            title: String, cat: String, amount: Long, essential: Boolean,
            frequency: Frequency = Frequency.MONTHLY, day: Int? = null, dow: DayOfWeek? = null,
        ) = PlanItem(
            title = title, amount = amount, type = EntryType.EXPENSE, frequency = frequency,
            dayOfMonth = day, dayOfWeek = dow, startDate = start,
            categoryId = categoryId(cat, EntryType.EXPENSE), isEssential = essential,
        )
        return listOf(
            income("Зарплата", "Зарплата", 40_000_000, 5),
            income("Подработка", "Подработка", 2_000_000, 20),
            expense("Жильё", "Жильё", 5_000_000, true, day = 1),
            expense("Кредит", "Кредиты", 8_000_000, true, day = 10),
            expense("Продукты", "Продукты", 500_000, true, Frequency.WEEKLY, dow = DayOfWeek.SATURDAY),
            expense("Транспорт", "Транспорт", 1_500_000, true, day = 1),
            expense("Коммунальные услуги", "Коммунальные услуги", 700_000, true, day = LAST_DAY_OF_MONTH),
            expense("Интернет", "Связь и интернет", 150_000, true, day = 12),
            expense("Подписки", "Подписки", 120_000, false, day = 15),
            expense("Развлечения", "Развлечения", 1_000_000, false, day = 25),
        )
    }

    fun transactions(today: LocalDate, categoryId: (name: String, type: EntryType) -> Long): List<Transaction> {
        fun exp(title: String, cat: String, amount: Long, daysAgo: Long) = Transaction(
            title = title, amount = amount, type = EntryType.EXPENSE,
            categoryId = categoryId(cat, EntryType.EXPENSE), date = today.minusDays(daysAgo),
        )
        return listOf(
            exp("Продукты", "Продукты", 185_000, 0),
            exp("Такси", "Транспорт", 35_000, 0),
            exp("Кафе", "Рестораны / кафе", 120_000, 1),
            exp("Продукты", "Продукты", 240_000, 2),
        )
    }
}
