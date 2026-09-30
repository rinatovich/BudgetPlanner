package com.budgetplanner.domain

import com.budgetplanner.domain.model.Category
import com.budgetplanner.domain.model.EntryType
import com.budgetplanner.domain.model.Frequency
import com.budgetplanner.domain.model.LAST_DAY_OF_MONTH
import com.budgetplanner.domain.model.PlanItem
import com.budgetplanner.domain.model.Transaction
import java.time.DayOfWeek
import java.time.LocalDate

object Ids {
    const val SALARY = 1L
    const val SIDE = 2L
    const val GROCERIES = 10L
    const val HOUSING = 11L
    const val CREDIT = 12L
    const val FUN = 13L
}

val testCategories = listOf(
    Category(Ids.SALARY, "Зарплата", "salary", EntryType.INCOME, true),
    Category(Ids.SIDE, "Подработка", "side", EntryType.INCOME, true),
    Category(Ids.GROCERIES, "Продукты", "cart", EntryType.EXPENSE, true, isEssentialDefault = true),
    Category(Ids.HOUSING, "Жильё", "home", EntryType.EXPENSE, true, isEssentialDefault = true),
    Category(Ids.CREDIT, "Кредиты", "credit", EntryType.EXPENSE, true, isEssentialDefault = true),
    Category(Ids.FUN, "Развлечения", "fun", EntryType.EXPENSE, true, isEssentialDefault = false),
)

val longAgo: LocalDate = LocalDate.of(2020, 1, 1)

fun monthlyIncome(amount: Long, day: Int, id: Long = 0, category: Long = Ids.SALARY, title: String = "Зарплата") =
    PlanItem(id = id, title = title, amount = amount, type = EntryType.INCOME, frequency = Frequency.MONTHLY,
        dayOfMonth = day, startDate = longAgo, categoryId = category)

fun monthlyExpense(
    amount: Long, day: Int, essential: Boolean = true, id: Long = 0,
    category: Long = Ids.HOUSING, title: String = "Расход",
) = PlanItem(id = id, title = title, amount = amount, type = EntryType.EXPENSE, frequency = Frequency.MONTHLY,
    dayOfMonth = day, startDate = longAgo, categoryId = category, isEssential = essential)

fun weeklyExpense(amount: Long, dow: DayOfWeek, essential: Boolean = true, category: Long = Ids.GROCERIES) =
    PlanItem(title = "Продукты", amount = amount, type = EntryType.EXPENSE, frequency = Frequency.WEEKLY,
        dayOfWeek = dow, startDate = longAgo, categoryId = category, isEssential = essential)

fun lastDayExpense(amount: Long) = monthlyExpense(amount, LAST_DAY_OF_MONTH)

fun expenseTx(amount: Long, date: LocalDate, category: Long = Ids.GROCERIES, id: Long = 0) =
    Transaction(id = id, title = "Факт", amount = amount, type = EntryType.EXPENSE, categoryId = category, date = date)

fun incomeTx(amount: Long, date: LocalDate, category: Long = Ids.SALARY) =
    Transaction(title = "Факт", amount = amount, type = EntryType.INCOME, categoryId = category, date = date)
