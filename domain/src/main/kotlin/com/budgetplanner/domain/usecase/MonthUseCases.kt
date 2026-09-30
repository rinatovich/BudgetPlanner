package com.budgetplanner.domain.usecase

import com.budgetplanner.domain.calc.BudgetCalculator
import com.budgetplanner.domain.calc.BudgetInputs
import com.budgetplanner.domain.calc.MonthSummary
import com.budgetplanner.domain.calc.ScenarioResult
import com.budgetplanner.domain.calc.ScenarioSimulator
import com.budgetplanner.domain.model.PlanItem
import com.budgetplanner.domain.repository.CategoryRepository
import com.budgetplanner.domain.repository.PlanRepository
import com.budgetplanner.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.time.LocalDate
import java.time.YearMonth

/** Поток «сегодняшней даты»; обновляется, если наступила полночь. */
object TodayTicker {
    val flow: Flow<LocalDate> = flow {
        while (true) {
            emit(LocalDate.now())
            delay(60_000)
        }
    }.distinctUntilChanged()
}

class ObserveBudgetInputs(
    private val plan: PlanRepository,
    private val transactions: TransactionRepository,
    private val categories: CategoryRepository,
) {
    operator fun invoke(): Flow<BudgetInputs> =
        combine(plan.observeAll(), transactions.observeAll(), categories.observeAll()) { p, t, c ->
            BudgetInputs(p, t, c)
        }
}

class ObserveMonthSummary(
    private val observeInputs: ObserveBudgetInputs,
    private val today: Flow<LocalDate> = TodayTicker.flow,
) {
    operator fun invoke(month: Flow<YearMonth>): Flow<MonthSummary> =
        combine(month, observeInputs(), today) { m, inputs, now ->
            BudgetCalculator.summarize(m, now, inputs)
        }.flowOn(Dispatchers.Default)
}

class SimulatePlanChange {
    operator fun invoke(month: YearMonth, inputs: BudgetInputs, draft: PlanItem, today: LocalDate = LocalDate.now()): ScenarioResult =
        ScenarioSimulator.simulate(month, today, inputs, draft)
}

