package com.budgetplanner.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetplanner.app.AppContainer
import com.budgetplanner.domain.calc.RecurrenceCalculator
import com.budgetplanner.domain.model.Category
import com.budgetplanner.domain.model.Currency
import com.budgetplanner.domain.model.EntryType
import com.budgetplanner.domain.model.Frequency
import com.budgetplanner.domain.model.PlanItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth

data class PlanRow(
    val item: PlanItem,
    val category: Category?,
    /** Сколько раз операция происходит в выбранном месяце. */
    val occurrences: Int,
    val monthTotal: Long,
)

data class PlanUiState(
    val month: YearMonth,
    val incomes: List<PlanRow>,
    val expenses: List<PlanRow>,
    val currency: Currency,
)

class PlanViewModel(private val container: AppContainer) : ViewModel() {

    val uiState: StateFlow<PlanUiState?> = combine(
        container.selectedMonth,
        container.planRepository.observeAll(),
        container.categoryRepository.observeAll(),
        container.settingsRepository.observe(),
    ) { month, items, categories, settings ->
        val byId = categories.associateBy { it.id }
        val rows = items.mapNotNull { item ->
            val count = RecurrenceCalculator.countIn(item, month)
            // Единоразовые операции других месяцев в плане месяца не показываем.
            if (item.frequency == Frequency.ONCE && YearMonth.from(item.startDate) != month) {
                null
            } else {
                PlanRow(item, byId[item.categoryId], count, item.amount * count)
            }
        }
        fun List<PlanRow>.ordered() = sortedWith(
            compareByDescending<PlanRow> { it.item.isActive }.thenByDescending { it.monthTotal },
        )
        PlanUiState(
            month = month,
            incomes = rows.filter { it.item.type == EntryType.INCOME }.ordered(),
            expenses = rows.filter { it.item.type == EntryType.EXPENSE }.ordered(),
            currency = settings.currency,
        ) as PlanUiState?
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setActive(id: Long, active: Boolean) {
        viewModelScope.launch { container.setPlanItemActive(id, active) }
    }

    fun delete(id: Long) {
        viewModelScope.launch { container.deletePlanItem(id) }
    }
}
