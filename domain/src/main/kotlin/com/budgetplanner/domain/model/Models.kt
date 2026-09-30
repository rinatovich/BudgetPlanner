package com.budgetplanner.domain.model

import java.time.DayOfWeek
import java.time.LocalDate

enum class Frequency { ONCE, DAILY, WEEKLY, MONTHLY, YEARLY }

enum class EntryType { INCOME, EXPENSE }

/** Значение [PlanItem.dayOfMonth] для «последний день месяца». */
const val LAST_DAY_OF_MONTH = 0

data class Category(
    val id: Long = 0,
    val name: String,
    /** Ключ иконки; UI сопоставляет его с конкретной иконкой. */
    val icon: String,
    val type: EntryType,
    val isDefault: Boolean = false,
    /** Подсказка для формы: считать ли расходы этой категории обязательными. */
    val isEssentialDefault: Boolean = false,
)

/**
 * Плановая (регулярная или единоразовая) операция — доход или расход.
 * Для [Frequency.ONCE] дата берётся из [startDate].
 * Для [Frequency.YEARLY] день и месяц берутся из [startDate].
 */
data class PlanItem(
    val id: Long = 0,
    val title: String,
    val amount: Long,
    val type: EntryType,
    val frequency: Frequency,
    val dayOfWeek: DayOfWeek? = null,
    /** 1..31 или [LAST_DAY_OF_MONTH]. */
    val dayOfMonth: Int? = null,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val isActive: Boolean = true,
    val categoryId: Long,
    val isEssential: Boolean = false,
    val note: String = "",
)

/** Фактическая операция — то, что реально произошло. */
data class Transaction(
    val id: Long = 0,
    val title: String,
    val amount: Long,
    val type: EntryType,
    val categoryId: Long,
    val date: LocalDate,
    val note: String = "",
)
