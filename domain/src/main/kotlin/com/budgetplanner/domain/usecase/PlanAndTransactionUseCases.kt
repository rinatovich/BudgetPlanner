package com.budgetplanner.domain.usecase

import com.budgetplanner.domain.model.PlanItem
import com.budgetplanner.domain.model.Transaction
import com.budgetplanner.domain.repository.PlanRepository
import com.budgetplanner.domain.repository.TransactionRepository

class SavePlanItem(private val repo: PlanRepository) {
    suspend operator fun invoke(item: PlanItem): Long = repo.upsert(item)
}

class DeletePlanItem(private val repo: PlanRepository) {
    suspend operator fun invoke(id: Long) = repo.delete(id)
}

class SetPlanItemActive(private val repo: PlanRepository) {
    suspend operator fun invoke(id: Long, active: Boolean) = repo.setActive(id, active)
}

class SaveTransaction(private val repo: TransactionRepository) {
    suspend operator fun invoke(transaction: Transaction): Long = repo.upsert(transaction)
}

class DeleteTransaction(private val repo: TransactionRepository) {
    suspend operator fun invoke(id: Long) = repo.delete(id)
}
