package com.budgetplanner.app

import android.content.Context
import androidx.room.Room
import com.budgetplanner.app.data.db.BudgetDatabase
import com.budgetplanner.app.data.repository.RoomCategoryRepository
import com.budgetplanner.app.data.repository.RoomPlanRepository
import com.budgetplanner.app.data.repository.RoomTransactionRepository
import com.budgetplanner.app.data.settings.DataStoreSettingsRepository
import com.budgetplanner.domain.repository.CategoryRepository
import com.budgetplanner.domain.repository.PlanRepository
import com.budgetplanner.domain.repository.SettingsRepository
import com.budgetplanner.domain.repository.TransactionRepository
import com.budgetplanner.domain.usecase.AddCategory
import com.budgetplanner.domain.usecase.ClearAllData
import com.budgetplanner.domain.usecase.DeleteCategory
import com.budgetplanner.domain.usecase.DeletePlanItem
import com.budgetplanner.domain.usecase.DeleteTransaction
import com.budgetplanner.domain.usecase.LoadDemoData
import com.budgetplanner.domain.usecase.ObserveBudgetInputs
import com.budgetplanner.domain.usecase.ObserveCategoriesByUsage
import com.budgetplanner.domain.usecase.ObserveMonthSummary
import com.budgetplanner.domain.usecase.SavePlanItem
import com.budgetplanner.domain.usecase.SaveTransaction
import com.budgetplanner.domain.usecase.SeedDefaultCategories
import com.budgetplanner.domain.usecase.SetPlanItemActive
import com.budgetplanner.domain.usecase.SimulatePlanChange
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.YearMonth

/** Ручной DI-контейнер: простой, без кодогенерации, легко заменить на Hilt/Koin. */
class AppContainer(context: Context) {
    private val database: BudgetDatabase =
        Room.databaseBuilder(context.applicationContext, BudgetDatabase::class.java, "budget.db").build()

    val categoryRepository: CategoryRepository = RoomCategoryRepository(database)
    val planRepository: PlanRepository = RoomPlanRepository(database)
    val transactionRepository: TransactionRepository = RoomTransactionRepository(database)
    val settingsRepository: SettingsRepository = DataStoreSettingsRepository(context)

    /** Месяц, который сейчас просматривает пользователь (общий для Главной, Плана и Операций). */
    val selectedMonth = MutableStateFlow(YearMonth.now())

    val seedDefaultCategories = SeedDefaultCategories(categoryRepository)
    val observeBudgetInputs = ObserveBudgetInputs(planRepository, transactionRepository, categoryRepository)
    val observeMonthSummary = ObserveMonthSummary(observeBudgetInputs)
    val observeCategoriesByUsage = ObserveCategoriesByUsage(categoryRepository, transactionRepository)
    val simulatePlanChange = SimulatePlanChange()

    val savePlanItem = SavePlanItem(planRepository)
    val deletePlanItem = DeletePlanItem(planRepository)
    val setPlanItemActive = SetPlanItemActive(planRepository)
    val saveTransaction = SaveTransaction(transactionRepository)
    val deleteTransaction = DeleteTransaction(transactionRepository)

    val addCategory = AddCategory(categoryRepository)
    val deleteCategory = DeleteCategory(categoryRepository)

    val loadDemoData = LoadDemoData(categoryRepository, planRepository, transactionRepository, seedDefaultCategories)
    val clearAllData = ClearAllData(planRepository, transactionRepository)
}
