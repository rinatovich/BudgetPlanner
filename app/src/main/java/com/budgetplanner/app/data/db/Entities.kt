package com.budgetplanner.app.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.budgetplanner.domain.model.EntryType
import com.budgetplanner.domain.model.Frequency

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String,
    val type: EntryType,
    val isDefault: Boolean,
    val isEssentialDefault: Boolean,
)

@Entity(tableName = "plan_items", indices = [Index("categoryId")])
data class PlanItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Long,
    val type: EntryType,
    val frequency: Frequency,
    /** ISO: 1 = понедельник … 7 = воскресенье. */
    val dayOfWeek: Int?,
    val dayOfMonth: Int?,
    val startEpochDay: Long,
    val endEpochDay: Long?,
    val isActive: Boolean,
    val categoryId: Long,
    val isEssential: Boolean,
    val note: String,
)

@Entity(tableName = "transactions", indices = [Index("categoryId"), Index("epochDay")])
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Long,
    val type: EntryType,
    val categoryId: Long,
    val epochDay: Long,
    val note: String,
)
