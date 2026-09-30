package com.budgetplanner.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetplanner.app.AppContainer
import com.budgetplanner.domain.model.AppSettings
import com.budgetplanner.domain.model.Category
import com.budgetplanner.domain.model.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek

class SettingsViewModel(private val container: AppContainer) : ViewModel() {

    val settings: StateFlow<AppSettings> = container.settingsRepository.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    val customCategories: StateFlow<List<Category>> = container.categoryRepository.observeAll()
        .map { list -> list.filter { !it.isDefault } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setTheme(theme: ThemeMode) {
        viewModelScope.launch { container.settingsRepository.setTheme(theme) }
    }

    fun setFirstDayOfWeek(day: DayOfWeek) {
        viewModelScope.launch { container.settingsRepository.setFirstDayOfWeek(day) }
    }

    fun loadDemoData() {
        viewModelScope.launch { container.loadDemoData() }
    }

    fun clearAllData() {
        viewModelScope.launch { container.clearAllData() }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch { container.deleteCategory(category) }
    }
}
