package com.budgetplanner.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CategoryEntity::class, PlanItemEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class BudgetDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun planDao(): PlanDao
    abstract fun transactionDao(): TransactionDao
}
