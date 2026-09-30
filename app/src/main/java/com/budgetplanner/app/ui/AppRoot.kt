package com.budgetplanner.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.budgetplanner.app.presentation.AppViewModelProvider
import com.budgetplanner.app.presentation.EntryViewModel
import com.budgetplanner.app.presentation.HomeViewModel
import com.budgetplanner.app.presentation.OnboardingViewModel
import com.budgetplanner.app.presentation.OperationsViewModel
import com.budgetplanner.app.presentation.PlanViewModel
import com.budgetplanner.app.presentation.RootViewModel
import com.budgetplanner.app.presentation.SettingsViewModel
import com.budgetplanner.app.ui.components.formatMoney
import com.budgetplanner.app.ui.home.HomeScreen
import com.budgetplanner.app.ui.onboarding.OnboardingScreen
import com.budgetplanner.app.ui.operations.OperationsScreen
import com.budgetplanner.app.ui.operations.TransactionSheet
import com.budgetplanner.app.ui.plan.PlanItemSheet
import com.budgetplanner.app.ui.plan.PlanScreen
import com.budgetplanner.app.ui.settings.SettingsScreen
import com.budgetplanner.app.ui.theme.BudgetTheme
import com.budgetplanner.domain.model.AppSettings
import com.budgetplanner.domain.model.EntryType
import com.budgetplanner.domain.model.PlanItem
import com.budgetplanner.domain.model.ThemeMode
import com.budgetplanner.domain.model.Transaction
import kotlinx.coroutines.launch

private enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    Home("home", "Главная", Icons.Rounded.Home),
    Plan("plan", "План", Icons.Rounded.CalendarMonth),
    Operations("operations", "Операции", Icons.Rounded.SwapVert),
    Settings("settings", "Настройки", Icons.Rounded.Settings),
}

/** Какая шторка сейчас открыта. */
private sealed interface Sheet {
    data class NewTransaction(val type: EntryType) : Sheet
    data class EditTransaction(val transaction: Transaction) : Sheet
    data class NewPlan(val type: EntryType) : Sheet
    data class EditPlan(val item: PlanItem) : Sheet
}

@Composable
fun AppRoot() {
    val root: RootViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val settings by root.settings.collectAsStateWithLifecycle()
    val current = settings

    BudgetTheme(current?.theme ?: ThemeMode.SYSTEM) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            when {
                current == null -> Unit
                !current.onboardingDone -> {
                    val onboarding: OnboardingViewModel = viewModel(factory = AppViewModelProvider.Factory)
                    OnboardingScreen(
                        currency = current.currency,
                        onFinish = { income, expenses -> onboarding.finish(income, expenses) {} },
                        onSkip = { onboarding.skip() },
                    )
                }
                else -> MainShell(root, current)
            }
        }
    }
}

@Composable
private fun MainShell(root: RootViewModel, settings: AppSettings) {
    val factory = AppViewModelProvider.Factory
    val homeVm: HomeViewModel = viewModel(factory = factory)
    val planVm: PlanViewModel = viewModel(factory = factory)
    val operationsVm: OperationsViewModel = viewModel(factory = factory)
    val entryVm: EntryViewModel = viewModel(factory = factory)
    val settingsVm: SettingsViewModel = viewModel(factory = factory)

    val month by root.month.collectAsStateWithLifecycle()
    val expenseCategories by entryVm.expenseCategories.collectAsStateWithLifecycle()
    val incomeCategories by entryVm.incomeCategories.collectAsStateWithLifecycle()
    val lastExpense by entryVm.lastExpense.collectAsStateWithLifecycle()

    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var sheet by remember { mutableStateOf<Sheet?>(null) }

    fun message(text: String) {
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            snackbar.showSnackbar(text)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            if (route != Tab.Settings.route) {
                FloatingActionButton(onClick = { sheet = Sheet.NewTransaction(EntryType.EXPENSE) }) {
                    Icon(Icons.Rounded.Add, contentDescription = "Добавить расход или доход")
                }
            }
        },
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = route == tab.route,
                        onClick = {
                            nav.navigate(tab.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(navController = nav, startDestination = Tab.Home.route) {
            composable(Tab.Home.route) {
                val state by homeVm.uiState.collectAsStateWithLifecycle()
                HomeScreen(
                    state = state,
                    month = month,
                    padding = padding,
                    onPrevious = { root.previousMonth() },
                    onNext = { root.nextMonth() },
                    onReset = { root.resetMonth() },
                    onAddPlan = { sheet = Sheet.NewPlan(it) },
                )
            }
            composable(Tab.Plan.route) {
                val state by planVm.uiState.collectAsStateWithLifecycle()
                PlanScreen(
                    state = state,
                    month = month,
                    padding = padding,
                    onPrevious = { root.previousMonth() },
                    onNext = { root.nextMonth() },
                    onReset = { root.resetMonth() },
                    onEdit = { sheet = Sheet.EditPlan(it) },
                    onAdd = { sheet = Sheet.NewPlan(it) },
                    onDelete = {
                        planVm.delete(it)
                        message("Удалено из плана")
                    },
                )
            }
            composable(Tab.Operations.route) {
                val state by operationsVm.uiState.collectAsStateWithLifecycle()
                OperationsScreen(
                    state = state,
                    month = month,
                    padding = padding,
                    onPrevious = { root.previousMonth() },
                    onNext = { root.nextMonth() },
                    onReset = { root.resetMonth() },
                    onEdit = { sheet = Sheet.EditTransaction(it) },
                    onDelete = {
                        operationsVm.delete(it)
                        message("Операция удалена")
                    },
                )
            }
            composable(Tab.Settings.route) {
                val custom by settingsVm.customCategories.collectAsStateWithLifecycle()
                SettingsScreen(
                    settings = settings,
                    customCategories = custom,
                    padding = padding,
                    onTheme = { settingsVm.setTheme(it) },
                    onFirstDay = { settingsVm.setFirstDayOfWeek(it) },
                    onLoadDemo = { settingsVm.loadDemoData() },
                    onClearData = { settingsVm.clearAllData() },
                    onDeleteCategory = { settingsVm.deleteCategory(it) },
                    onMessage = { message(it) },
                )
            }
        }
    }

    val open = sheet
    if (open != null) {
        key(open) {
            when (open) {
                is Sheet.NewTransaction, is Sheet.EditTransaction -> {
                    val editing = (open as? Sheet.EditTransaction)?.transaction
                    TransactionSheet(
                        initial = editing,
                        initialType = (open as? Sheet.NewTransaction)?.type ?: EntryType.EXPENSE,
                        expenseCategories = expenseCategories,
                        incomeCategories = incomeCategories,
                        lastExpense = lastExpense,
                        lastExpenseCategoryName = expenseCategories.firstOrNull { it.id == lastExpense?.categoryId }?.name,
                        currency = settings.currency,
                        onSave = { tx ->
                            entryVm.saveTransaction(tx)
                            val sign = if (tx.type == EntryType.INCOME) "Доход" else "Расход"
                            message("$sign записан: ${formatMoney(tx.amount, settings.currency)}")
                        },
                        onDelete = { tx ->
                            entryVm.deleteTransaction(tx.id)
                            message("Операция удалена")
                        },
                        onCreateCategory = { name, type, onCreated -> entryVm.addCategory(name, type, onCreated) },
                        onPlanInstead = { type -> sheet = Sheet.NewPlan(type) },
                        onDismiss = { sheet = null },
                    )
                }
                is Sheet.NewPlan, is Sheet.EditPlan -> {
                    val editing = (open as? Sheet.EditPlan)?.item
                    val type = editing?.type ?: (open as Sheet.NewPlan).type
                    PlanItemSheet(
                        type = type,
                        initial = editing,
                        categories = if (type == EntryType.EXPENSE) expenseCategories else incomeCategories,
                        currency = settings.currency,
                        firstDayOfWeek = settings.firstDayOfWeek,
                        defaultStartDate = month.atDay(1),
                        simulate = { draft -> entryVm.simulate(draft) },
                        onSave = { item ->
                            entryVm.savePlanItem(item)
                            message("План обновлён")
                        },
                        onDelete = { item ->
                            entryVm.deletePlanItem(item.id)
                            message("Удалено из плана")
                        },
                        onCreateCategory = { name, onCreated -> entryVm.addCategory(name, type, onCreated) },
                        onDismiss = { sheet = null },
                    )
                }
            }
        }
    }
}
