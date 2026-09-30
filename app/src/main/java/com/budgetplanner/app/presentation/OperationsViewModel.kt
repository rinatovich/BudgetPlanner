package com.budgetplanner.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetplanner.app.AppContainer
import com.budgetplanner.domain.model.Category
import com.budgetplanner.domain.model.Currency
import com.budgetplanner.domain.model.EntryType
import com.budgetplanner.domain.model.Transaction
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class DayGroup(
    val date: LocalDate,
    val transactions: List<Transaction>,
    /** Итог за день со знаком (+ доходы, − расходы). */
    val total: Long,
)

data class OperationsUiState(
    val month: YearMonth,
    val groups: List<DayGroup>,
    val categories: Map<Long, Category>,
    val currency: Currency,
)

class OperationsViewModel(private val container: AppContainer) : ViewModel() {

    val uiState: StateFlow<OperationsUiState?> = combine(
        container.selectedMonth,
        container.transactionRepository.observeAll(),
        container.categoryRepository.observeAll(),
        container.settingsRepository.observe(),
    ) { month, transactions, categories, settings ->
        val groups = transactions
            .filter { YearMonth.from(it.date) == month }
            .groupBy { it.date }
            .toSortedMap(compareByDescending<LocalDate> { it })
            .map { (date, list) ->
                DayGroup(
                    date = date,
                    transactions = list.sortedByDescending { it.id },
                    total = list.sumOf { if (it.type == EntryType.INCOME) it.amount else -it.amount },
                )
            }
        OperationsUiState(month, groups, categories.associateBy { it.id }, settings.currency) as OperationsUiState?
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun delete(id: Long) {
        viewModelScope.launch { container.deleteTransaction(id) }
    }
}
