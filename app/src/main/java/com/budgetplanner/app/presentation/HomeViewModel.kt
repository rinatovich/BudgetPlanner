package com.budgetplanner.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetplanner.app.AppContainer
import com.budgetplanner.domain.calc.MonthSummary
import com.budgetplanner.domain.model.Category
import com.budgetplanner.domain.model.Currency
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val summary: MonthSummary,
    val categories: Map<Long, Category>,
    val currency: Currency,
)

/** null — данные ещё считаются. */
class HomeViewModel(container: AppContainer) : ViewModel() {

    val uiState: StateFlow<HomeUiState?> = combine(
        container.observeMonthSummary(container.selectedMonth),
        container.categoryRepository.observeAll(),
        container.settingsRepository.observe(),
    ) { summary, categories, settings ->
        HomeUiState(summary, categories.associateBy { it.id }, settings.currency) as HomeUiState?
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
