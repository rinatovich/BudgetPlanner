package com.budgetplanner.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY id")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY id")
    suspend fun getAll(): List<CategoryEntity>

    @Insert
    suspend fun insert(entity: CategoryEntity): Long

    @Insert
    suspend fun insertAll(entities: List<CategoryEntity>)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface PlanDao {
    @Query("SELECT * FROM plan_items ORDER BY id")
    fun observeAll(): Flow<List<PlanItemEntity>>

    @Upsert
    suspend fun upsert(entity: PlanItemEntity): Long

    @Insert
    suspend fun insertAll(entities: List<PlanItemEntity>)

    @Query("DELETE FROM plan_items WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE plan_items SET isActive = :active WHERE id = :id")
    suspend fun setActive(id: Long, active: Boolean)

    @Query("DELETE FROM plan_items")
    suspend fun deleteAll()

    @Query("UPDATE plan_items SET categoryId = :to WHERE categoryId = :from")
    suspend fun reassign(from: Long, to: Long)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY epochDay DESC, id DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Upsert
    suspend fun upsert(entity: TransactionEntity): Long

    @Insert
    suspend fun insertAll(entities: List<TransactionEntity>)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Query("UPDATE transactions SET categoryId = :to WHERE categoryId = :from")
    suspend fun reassign(from: Long, to: Long)
}
