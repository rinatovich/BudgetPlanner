package com.budgetplanner.app.presentation

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.budgetplanner.app.AppContainer
import com.budgetplanner.app.BudgetApp

private fun CreationExtras.container(): AppContainer =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BudgetApp).container

object AppViewModelProvider {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer { RootViewModel(container()) }
        initializer { HomeViewModel(container()) }
        initializer { PlanViewModel(container()) }
        initializer { OperationsViewModel(container()) }
        initializer { EntryViewModel(container()) }
        initializer { SettingsViewModel(container()) }
        initializer { OnboardingViewModel(container()) }
    }
}
