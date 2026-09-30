package com.budgetplanner.app.data.db

import com.budgetplanner.domain.model.Category
import com.budgetplanner.domain.model.PlanItem
import com.budgetplanner.domain.model.Transaction
import java.time.DayOfWeek
import java.time.LocalDate

fun CategoryEntity.toDomain() = Category(id, name, icon, type, isDefault, isEssentialDefault)

fun Category.toEntity() = CategoryEntity(id, name, icon, type, isDefault, isEssentialDefault)

fun PlanItemEntity.toDomain() = PlanItem(
    id = id,
    title = title,
    amount = amount,
    type = type,
    frequency = frequency,
    dayOfWeek = dayOfWeek?.let { DayOfWeek.of(it) },
    dayOfMonth = dayOfMonth,
    startDate = LocalDate.ofEpochDay(startEpochDay),
    endDate = endEpochDay?.let { LocalDate.ofEpochDay(it) },
    isActive = isActive,
    categoryId = categoryId,
    isEssential = isEssential,
    note = note,
)

fun PlanItem.toEntity() = PlanItemEntity(
    id = id,
    title = title,
    amount = amount,
    type = type,
    frequency = frequency,
    dayOfWeek = dayOfWeek?.value,
    dayOfMonth = dayOfMonth,
    startEpochDay = startDate.toEpochDay(),
    endEpochDay = endDate?.toEpochDay(),
    isActive = isActive,
    categoryId = categoryId,
    isEssential = isEssential,
    note = note,
)

fun TransactionEntity.toDomain() = Transaction(
    id = id,
    title = title,
    amount = amount,
    type = type,
    categoryId = categoryId,
    date = LocalDate.ofEpochDay(epochDay),
    note = note,
)

fun Transaction.toEntity() = TransactionEntity(
    id = id,
    title = title,
    amount = amount,
    type = type,
    categoryId = categoryId,
    epochDay = date.toEpochDay(),
    note = note,
)
