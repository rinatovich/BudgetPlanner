package com.budgetplanner.domain.repository

import com.budgetplanner.domain.model.AppSettings
import com.budgetplanner.domain.model.Category
import com.budgetplanner.domain.model.Currency
import com.budgetplanner.domain.model.PlanItem
import com.budgetplanner.domain.model.ThemeMode
import com.budgetplanner.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek

interface CategoryRepository {
    fun observeAll(): Flow<List<Category>>
    suspend fun getAll(): List<Category>
    suspend fun insert(category: Category): Long
    suspend fun insertAll(categories: List<Category>)
    suspend fun delete(id: Long)
    /** Переносит плановые и фактические операции из одной категории в другую. */
    suspend fun reassign(fromCategoryId: Long, toCategoryId: Long)
}

interface PlanRepository {
    fun observeAll(): Flow<List<PlanItem>>
    suspend fun upsert(item: PlanItem): Long
    suspend fun insertAll(items: List<PlanItem>)
    suspend fun delete(id: Long)
    suspend fun setActive(id: Long, active: Boolean)
    suspend fun deleteAll()
}

interface TransactionRepository {
    fun observeAll(): Flow<List<Transaction>>
    suspend fun upsert(transaction: Transaction): Long
    suspend fun insertAll(transactions: List<Transaction>)
    suspend fun delete(id: Long)
    suspend fun deleteAll()
}

interface SettingsRepository {
    fun observe(): Flow<AppSettings>
    suspend fun setTheme(theme: ThemeMode)
    suspend fun setFirstDayOfWeek(day: DayOfWeek)
    suspend fun setCurrency(currency: Currency)
    suspend fun setOnboardingDone(done: Boolean)
}
