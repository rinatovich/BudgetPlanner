package com.budgetplanner.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetplanner.app.AppContainer
import com.budgetplanner.domain.model.AppSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.YearMonth

/** Настройки приложения и выбор месяца — общие для всех экранов. */
class RootViewModel(private val container: AppContainer) : ViewModel() {

    /** null — настройки ещё загружаются. */
    val settings: StateFlow<AppSettings?> = container.settingsRepository.observe()
        .map<AppSettings, AppSettings?> { it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val month: StateFlow<YearMonth> = container.selectedMonth

    fun previousMonth() = container.selectedMonth.update { it.minusMonths(1) }

    fun nextMonth() = container.selectedMonth.update { it.plusMonths(1) }

    fun resetMonth() {
        container.selectedMonth.value = YearMonth.now()
    }
}
