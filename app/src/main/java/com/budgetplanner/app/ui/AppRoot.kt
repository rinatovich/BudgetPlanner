package com.budgetplanner.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
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
import com.budgetplanner.app.ui.components.IosEasing
import com.budgetplanner.app.ui.components.ToastData
import com.budgetplanner.app.ui.components.ToastPill
import com.budgetplanner.app.ui.components.bouncyClickable
import com.budgetplanner.app.ui.components.formatMoney
import com.budgetplanner.app.ui.components.rememberTick
import com.budgetplanner.app.ui.home.HomeScreen
import com.budgetplanner.app.ui.onboarding.OnboardingScreen
import com.budgetplanner.app.ui.operations.OperationsScreen
import com.budgetplanner.app.ui.operations.TransactionSheet
import com.budgetplanner.app.ui.plan.PlanItemSheet
import com.budgetplanner.app.ui.plan.PlanScreen
import com.budgetplanner.app.ui.settings.SettingsScreen
import com.budgetplanner.app.ui.theme.AppText
import com.budgetplanner.app.ui.theme.BudgetTheme
import com.budgetplanner.domain.model.AppSettings
import com.budgetplanner.domain.model.EntryType
import com.budgetplanner.domain.model.PlanItem
import com.budgetplanner.domain.model.ThemeMode
import com.budgetplanner.domain.model.Transaction
import kotlinx.coroutines.delay

private const val ROUTE_SETTINGS = "settings"

private enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    Home("home", "Обзор", Icons.Rounded.PieChart),
    Plan("plan", "План", Icons.Rounded.CalendarMonth),
    Operations("operations", "Операции", Icons.Rounded.SwapVert),
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
    val selectedTab = Tab.entries.firstOrNull { it.route == route } ?: Tab.Home

    var sheet by remember { mutableStateOf<Sheet?>(null) }

    var toast by remember { mutableStateOf<ToastData?>(null) }
    LaunchedEffect(toast?.id) {
        if (toast != null) {
            delay(4000)
            toast = null
        }
    }
    fun showToast(text: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
        toast = ToastData(System.nanoTime(), text, actionLabel, onAction)
    }

    Box(Modifier.fillMaxSize()) {
        NavHost(
            navController = nav,
            startDestination = Tab.Home.route,
            modifier = Modifier.fillMaxSize(),
            enterTransition = {
                if (targetState.destination.route == ROUTE_SETTINGS) {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(380, easing = IosEasing))
                } else {
                    fadeIn(tween(260, easing = IosEasing)) + scaleIn(initialScale = 0.985f, animationSpec = tween(260, easing = IosEasing))
                }
            },
            exitTransition = {
                if (targetState.destination.route == ROUTE_SETTINGS) {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Start,
                        tween(380, easing = IosEasing),
                        targetOffset = { it / 4 },
                    )
                } else {
                    fadeOut(tween(140))
                }
            },
            popEnterTransition = {
                if (initialState.destination.route == ROUTE_SETTINGS) {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.End,
                        tween(380, easing = IosEasing),
                        initialOffset = { it / 4 },
                    )
                } else {
                    fadeIn(tween(260, easing = IosEasing))
                }
            },
            popExitTransition = {
                if (initialState.destination.route == ROUTE_SETTINGS) {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(380, easing = IosEasing))
                } else {
                    fadeOut(tween(140))
                }
            },
        ) {
            composable(Tab.Home.route) {
                val state by homeVm.uiState.collectAsStateWithLifecycle()
                HomeScreen(
                    state = state,
                    month = month,
                    onPrevious = { root.previousMonth() },
                    onNext = { root.nextMonth() },
                    onReset = { root.resetMonth() },
                    onAddPlan = { sheet = Sheet.NewPlan(it) },
                    onOpenSettings = { nav.navigate(ROUTE_SETTINGS) { launchSingleTop = true } },
                )
            }
            composable(Tab.Plan.route) {
                val state by planVm.uiState.collectAsStateWithLifecycle()
                PlanScreen(
                    state = state,
                    month = month,
                    onPrevious = { root.previousMonth() },
                    onNext = { root.nextMonth() },
                    onReset = { root.resetMonth() },
                    onEdit = { sheet = Sheet.EditPlan(it) },
                    onAdd = { sheet = Sheet.NewPlan(it) },
                    onDelete = { item ->
                        planVm.delete(item.id)
                        showToast("Удалено из плана", "Отменить") { entryVm.savePlanItem(item.copy(id = 0L)) }
                    },
                )
            }
            composable(Tab.Operations.route) {
                val state by operationsVm.uiState.collectAsStateWithLifecycle()
                OperationsScreen(
                    state = state,
                    month = month,
                    onPrevious = { root.previousMonth() },
                    onNext = { root.nextMonth() },
                    onReset = { root.resetMonth() },
                    onEdit = { sheet = Sheet.EditTransaction(it) },
                    onDelete = { tx ->
                        operationsVm.delete(tx.id)
                        showToast("Операция удалена", "Отменить") { entryVm.saveTransaction(tx.copy(id = 0L)) }
                    },
                )
            }
            composable(ROUTE_SETTINGS) {
                val custom by settingsVm.customCategories.collectAsStateWithLifecycle()
                SettingsScreen(
                    settings = settings,
                    customCategories = custom,
                    onBack = { nav.popBackStack() },
                    onTheme = { settingsVm.setTheme(it) },
                    onFirstDay = { settingsVm.setFirstDayOfWeek(it) },
                    onLoadDemo = { settingsVm.loadDemoData() },
                    onClearData = { settingsVm.clearAllData() },
                    onDeleteCategory = { settingsVm.deleteCategory(it) },
                    onMessage = { showToast(it) },
                )
            }
        }

        // Плавающий таб-бар: прячется на экране настроек.
        AnimatedVisibility(
            visible = route != ROUTE_SETTINGS,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(tween(320, easing = IosEasing)) { it } + fadeIn(tween(220)),
            exit = slideOutVertically(tween(260, easing = IosEasing)) { it } + fadeOut(tween(160)),
        ) {
            FloatingTabBar(
                selected = selectedTab,
                onSelect = { tab ->
                    nav.navigate(tab.route) {
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onAdd = { sheet = Sheet.NewTransaction(EntryType.EXPENSE) },
            )
        }

        // Тост сверху: выезжает и уезжает.
        AnimatedContent(
            targetState = toast,
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 8.dp),
            transitionSpec = {
                (slideInVertically(spring(dampingRatio = 0.75f, stiffness = 380f)) { -it * 2 } + fadeIn(tween(160))) togetherWith
                    (slideOutVertically(tween(220, easing = IosEasing)) { -it * 2 } + fadeOut(tween(160))) using
                    SizeTransform(clip = false)
            },
            label = "toast",
        ) { data ->
            if (data != null) {
                ToastPill(data) {
                    data.onAction?.invoke()
                    toast = null
                }
            } else {
                Box(Modifier.size(0.dp))
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
                            showToast("$sign записан · ${formatMoney(tx.amount, settings.currency)}")
                        },
                        onDelete = { tx ->
                            entryVm.deleteTransaction(tx.id)
                            showToast("Операция удалена", "Отменить") { entryVm.saveTransaction(tx.copy(id = 0L)) }
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
                            showToast("План обновлён")
                        },
                        onDelete = { item ->
                            entryVm.deletePlanItem(item.id)
                            showToast("Удалено из плана", "Отменить") { entryVm.savePlanItem(item.copy(id = 0L)) }
                        },
                        onCreateCategory = { name, onCreated -> entryVm.addCategory(name, type, onCreated) },
                        onDismiss = { sheet = null },
                    )
                }
            }
        }
    }
}

/**
 * Плавающая «капсула» с вкладками и отдельная круглая кнопка «+» рядом — как в новых приложениях Apple.
 * Подсветка выбранной вкладки плавно переезжает пружиной.
 */
@Composable
private fun FloatingTabBar(selected: Tab, onSelect: (Tab) -> Unit, onAdd: () -> Unit) {
    val c = BudgetTheme.colors
    val tick = rememberTick()
    Row(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BoxWithConstraints(
            Modifier
                .weight(1f)
                .height(64.dp)
                .shadow(14.dp, CircleShape, clip = false, ambientColor = Color.Black.copy(alpha = 0.10f), spotColor = Color.Black.copy(alpha = 0.18f))
                .clip(CircleShape)
                .background(c.bar)
                .border(0.5.dp, c.separator, CircleShape)
                .padding(4.dp),
        ) {
            val itemWidth = maxWidth / Tab.entries.size
            val x by animateDpAsState(
                targetValue = itemWidth * selected.ordinal,
                animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f),
                label = "tab-highlight",
            )
            Box(
                Modifier.offset(x = x).width(itemWidth).fillMaxHeight().clip(CircleShape).background(c.fill),
            )
            Row(Modifier.fillMaxSize()) {
                Tab.entries.forEach { tab ->
                    val isSelected = tab == selected
                    val tint by animateColorAsState(if (isSelected) c.blue else c.secondaryLabel, tween(200), label = "tab-tint")
                    Column(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                                if (!isSelected) {
                                    tick()
                                    onSelect(tab)
                                }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(tab.icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
                        Text(tab.label, style = AppText.caption2, color = tint, maxLines = 1)
                    }
                }
            }
        }

        Box(
            Modifier
                .size(64.dp)
                .bouncyClickable(scale = 0.9f, tick = true, onClick = onAdd)
                .shadow(14.dp, CircleShape, clip = false, ambientColor = c.blue.copy(alpha = 0.2f), spotColor = c.blue.copy(alpha = 0.4f))
                .clip(CircleShape)
                .background(c.blue),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Добавить расход или доход", tint = Color.White, modifier = Modifier.size(30.dp))
        }
    }
}
