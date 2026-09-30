package com.budgetplanner.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetplanner.app.AppContainer
import com.budgetplanner.domain.model.EntryType
import com.budgetplanner.domain.model.Frequency
import com.budgetplanner.domain.model.PlanItem
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.YearMonth

data class OnboardingIncome(
    val title: String,
    val amount: Long,
    val frequency: Frequency,
    val dayOfMonth: Int,
    val dayOfWeek: DayOfWeek,
)

/** Быстрый расход из второго шага: категория + сумма. */
data class OnboardingExpense(val categoryName: String, val amount: Long)

class OnboardingViewModel(private val container: AppContainer) : ViewModel() {

    fun finish(income: OnboardingIncome?, expenses: List<OnboardingExpense>, onDone: () -> Unit) {
        viewModelScope.launch {
            container.seedDefaultCategories()
            val categories = container.categoryRepository.getAll()
            val start = YearMonth.now().atDay(1)

            income?.takeIf { it.amount > 0 }?.let { inc ->
                val category = categories.firstOrNull { it.type == EntryType.INCOME && it.name == inc.title }
                    ?: categories.firstOrNull { it.type == EntryType.INCOME }
                if (category != null) {
                    container.savePlanItem(
                        PlanItem(
                            title = inc.title,
                            amount = inc.amount,
                            type = EntryType.INCOME,
                            frequency = inc.frequency,
                            dayOfWeek = if (inc.frequency == Frequency.WEEKLY) inc.dayOfWeek else null,
                            dayOfMonth = if (inc.frequency == Frequency.MONTHLY) inc.dayOfMonth else null,
                            startDate = start,
                            categoryId = category.id,
                        ),
                    )
                }
            }

            expenses.filter { it.amount > 0 }.forEach { exp ->
                val category = categories.firstOrNull { it.type == EntryType.EXPENSE && it.name == exp.categoryName }
                    ?: return@forEach
                val weekly = exp.categoryName == "Продукты"
                container.savePlanItem(
                    PlanItem(
                        title = category.name,
                        amount = exp.amount,
                        type = EntryType.EXPENSE,
                        frequency = if (weekly) Frequency.WEEKLY else Frequency.MONTHLY,
                        dayOfWeek = if (weekly) DayOfWeek.SATURDAY else null,
                        dayOfMonth = if (weekly) null else if (exp.categoryName == "Кредиты") 10 else 1,
                        startDate = start,
                        categoryId = category.id,
                        isEssential = category.isEssentialDefault,
                    ),
                )
            }

            container.settingsRepository.setOnboardingDone(true)
            onDone()
        }
    }

    fun skip() {
        viewModelScope.launch { container.settingsRepository.setOnboardingDone(true) }
    }
}
