package com.budgetplanner.domain.usecase

import com.budgetplanner.domain.demo.DemoData
import com.budgetplanner.domain.model.EntryType
import com.budgetplanner.domain.repository.CategoryRepository
import com.budgetplanner.domain.repository.PlanRepository
import com.budgetplanner.domain.repository.TransactionRepository
import java.time.LocalDate

class LoadDemoData(
    private val categories: CategoryRepository,
    private val plan: PlanRepository,
    private val transactions: TransactionRepository,
    private val seed: SeedDefaultCategories,
) {
    suspend operator fun invoke(today: LocalDate = LocalDate.now()) {
        seed()
        val all = categories.getAll()
        val lookup = { name: String, type: EntryType ->
            (all.firstOrNull { it.type == type && it.name == name } ?: all.first { it.type == type }).id
        }
        plan.insertAll(DemoData.planItems(today, lookup))
        transactions.insertAll(DemoData.transactions(today, lookup))
    }
}

/** Удаляет плановые и фактические операции. Категории остаются. */
class ClearAllData(
    private val plan: PlanRepository,
    private val transactions: TransactionRepository,
) {
    suspend operator fun invoke() {
        plan.deleteAll()
        transactions.deleteAll()
    }
}
