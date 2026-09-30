package com.budgetplanner.domain.usecase

import com.budgetplanner.domain.model.Category
import com.budgetplanner.domain.model.DefaultCategories
import com.budgetplanner.domain.model.EntryType
import com.budgetplanner.domain.repository.CategoryRepository
import com.budgetplanner.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SeedDefaultCategories(private val repo: CategoryRepository) {
    private val mutex = Mutex()

    suspend operator fun invoke() = mutex.withLock {
        if (repo.getAll().isEmpty()) repo.insertAll(DefaultCategories.all)
    }
}

/** Категории заданного типа: чаще используемые — выше. */
class ObserveCategoriesByUsage(
    private val categories: CategoryRepository,
    private val transactions: TransactionRepository,
) {
    operator fun invoke(type: EntryType): Flow<List<Category>> =
        combine(categories.observeAll(), transactions.observeAll()) { cats, txs ->
            val usage = txs.groupingBy { it.categoryId }.eachCount()
            cats.filter { it.type == type }
                .sortedWith(compareByDescending<Category> { usage[it.id] ?: 0 }.thenBy { it.id })
        }
}

class AddCategory(private val repo: CategoryRepository) {
    /** Возвращает id новой (или уже существующей с таким именем) категории. */
    suspend operator fun invoke(name: String, type: EntryType): Long? {
        val clean = name.trim()
        if (clean.isEmpty()) return null
        val existing = repo.getAll().firstOrNull { it.type == type && it.name.equals(clean, ignoreCase = true) }
        if (existing != null) return existing.id
        return repo.insert(Category(name = clean, icon = "label", type = type, isDefault = false))
    }
}

class DeleteCategory(private val repo: CategoryRepository) {
    /** Пользовательскую категорию можно удалить; её операции переносятся в «Другое». */
    suspend operator fun invoke(category: Category) {
        if (category.isDefault) return
        val all = repo.getAll()
        val fallback = all.firstOrNull { it.type == category.type && it.name == DefaultCategories.OTHER }
        if (fallback != null) repo.reassign(category.id, fallback.id)
        repo.delete(category.id)
    }
}
