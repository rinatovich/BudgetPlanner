package com.budgetplanner.domain.calc

import com.budgetplanner.domain.model.PlanItem
import java.time.LocalDate
import java.time.YearMonth

data class ScenarioResult(val freeBefore: Long, val freeAfter: Long) {
    val delta: Long get() = freeAfter - freeBefore
}

/**
 * «Что если»: показывает влияние новой (или изменённой) плановой операции на свободный остаток,
 * не сохраняя её. Основа для будущего полноценного сценарного режима.
 */
object ScenarioSimulator {
    fun simulate(
        month: YearMonth,
        today: LocalDate,
        inputs: BudgetInputs,
        draft: PlanItem,
    ): ScenarioResult {
        val before = BudgetCalculator.summarize(month, today, inputs)
        val newPlan =
            if (draft.id != 0L && inputs.planItems.any { it.id == draft.id }) {
                inputs.planItems.map { if (it.id == draft.id) draft else it }
            } else {
                inputs.planItems + draft
            }
        val after = BudgetCalculator.summarize(month, today, inputs.copy(planItems = newPlan))
        return ScenarioResult(before.free, after.free)
    }
}
