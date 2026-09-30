package com.budgetplanner.app.data.repository

import androidx.room.withTransaction
import com.budgetplanner.app.data.db.BudgetDatabase
import com.budgetplanner.app.data.db.toDomain
import com.budgetplanner.app.data.db.toEntity
import com.budgetplanner.domain.model.Category
import com.budgetplanner.domain.model.PlanItem
import com.budgetplanner.domain.model.Transaction
import com.budgetplanner.domain.repository.CategoryRepository
import com.budgetplanner.domain.repository.PlanRepository
import com.budgetplanner.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCategoryRepository(private val db: BudgetDatabase) : CategoryRepository {
    private val dao = db.categoryDao()

    override fun observeAll(): Flow<List<Category>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getAll(): List<Category> = dao.getAll().map { it.toDomain() }

    override suspend fun insert(category: Category): Long = dao.insert(category.toEntity())

    override suspend fun insertAll(categories: List<Category>) = dao.insertAll(categories.map { it.toEntity() })

    override suspend fun delete(id: Long) = dao.delete(id)

    override suspend fun reassign(fromCategoryId: Long, toCategoryId: Long) {
        db.withTransaction {
            db.planDao().reassign(fromCategoryId, toCategoryId)
            db.transactionDao().reassign(fromCategoryId, toCategoryId)
        }
    }
}

class RoomPlanRepository(db: BudgetDatabase) : PlanRepository {
    private val dao = db.planDao()

    override fun observeAll(): Flow<List<PlanItem>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun upsert(item: PlanItem): Long {
        val result = dao.upsert(item.toEntity())
        // Room возвращает -1 при обновлении существующей строки.
        return if (result > 0) result else item.id
    }

    override suspend fun insertAll(items: List<PlanItem>) = dao.insertAll(items.map { it.toEntity() })

    override suspend fun delete(id: Long) = dao.delete(id)

    override suspend fun setActive(id: Long, active: Boolean) = dao.setActive(id, active)

    override suspend fun deleteAll() = dao.deleteAll()
}

class RoomTransactionRepository(db: BudgetDatabase) : TransactionRepository {
    private val dao = db.transactionDao()

    override fun observeAll(): Flow<List<Transaction>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun upsert(transaction: Transaction): Long {
        val result = dao.upsert(transaction.toEntity())
        return if (result > 0) result else transaction.id
    }

    override suspend fun insertAll(transactions: List<Transaction>) = dao.insertAll(transactions.map { it.toEntity() })

    override suspend fun delete(id: Long) = dao.delete(id)

    override suspend fun deleteAll() = dao.deleteAll()
}
