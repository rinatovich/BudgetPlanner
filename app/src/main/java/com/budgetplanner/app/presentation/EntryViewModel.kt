package com.budgetplanner.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetplanner.app.AppContainer
import com.budgetplanner.domain.calc.BudgetInputs
import com.budgetplanner.domain.calc.ScenarioResult
import com.budgetplanner.domain.model.Category
import com.budgetplanner.domain.model.EntryType
import com.budgetplanner.domain.model.Frequency
import com.budgetplanner.domain.model.PlanItem
import com.budgetplanner.domain.model.Transaction
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth

/** Всё, что нужно шторкам добавления и редактирования. */
class EntryViewModel(private val container: AppContainer) : ViewModel() {

    val expenseCategories: StateFlow<List<Category>> =
        container.observeCategoriesByUsage(EntryType.EXPENSE)
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val incomeCategories: StateFlow<List<Category>> =
        container.observeCategoriesByUsage(EntryType.INCOME)
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val inputs: StateFlow<BudgetInputs?> = container.observeBudgetInputs()
        .map<BudgetInputs, BudgetInputs?> { it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Последний фактический расход — для кнопки «Повторить». */
    val lastExpense: StateFlow<Transaction?> = container.transactionRepository.observeAll()
        .map { list -> list.filter { it.type == EntryType.EXPENSE }.maxWithOrNull(compareBy<Transaction>({ it.date }, { it.id })) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun saveTransaction(transaction: Transaction) {
        viewModelScope.launch { container.saveTransaction(transaction) }
    }

    fun savePlanItem(item: PlanItem) {
        viewModelScope.launch { container.savePlanItem(item) }
    }

    fun deletePlanItem(id: Long) {
        viewModelScope.launch { container.deletePlanItem(id) }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch { container.deleteTransaction(id) }
    }

    fun addCategory(name: String, type: EntryType, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val id = container.addCategory(name, type)
            if (id != null) onCreated(id)
        }
    }

    /** «Что если»: как изменится свободный остаток, если сохранить этот черновик. */
    fun simulate(draft: PlanItem): ScenarioResult? {
        val current = inputs.value ?: return null
        val month = if (draft.frequency == Frequency.ONCE) YearMonth.from(draft.startDate) else container.selectedMonth.value
        return container.simulatePlanChange(month, current, draft)
    }
}
