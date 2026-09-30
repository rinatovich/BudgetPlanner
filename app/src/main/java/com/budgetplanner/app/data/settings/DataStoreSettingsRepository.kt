package com.budgetplanner.app.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.budgetplanner.domain.model.AppSettings
import com.budgetplanner.domain.model.Currency
import com.budgetplanner.domain.model.ThemeMode
import com.budgetplanner.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek

private val Context.settingsStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreSettingsRepository(context: Context) : SettingsRepository {
    private val store = context.applicationContext.settingsStore

    private val themeKey = stringPreferencesKey("theme")
    private val currencyKey = stringPreferencesKey("currency")
    private val firstDayKey = intPreferencesKey("first_day_of_week")
    private val onboardingKey = booleanPreferencesKey("onboarding_done")

    override fun observe(): Flow<AppSettings> = store.data.map { p ->
        AppSettings(
            currency = p[currencyKey]?.let { name -> Currency.entries.firstOrNull { it.name == name } } ?: Currency.UZS,
            firstDayOfWeek = DayOfWeek.of(p[firstDayKey] ?: DayOfWeek.MONDAY.value),
            theme = p[themeKey]?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } } ?: ThemeMode.SYSTEM,
            onboardingDone = p[onboardingKey] ?: false,
        )
    }

    override suspend fun setTheme(theme: ThemeMode) {
        store.edit { it[themeKey] = theme.name }
    }

    override suspend fun setFirstDayOfWeek(day: DayOfWeek) {
        store.edit { it[firstDayKey] = day.value }
    }

    override suspend fun setCurrency(currency: Currency) {
        store.edit { it[currencyKey] = currency.name }
    }

    override suspend fun setOnboardingDone(done: Boolean) {
        store.edit { it[onboardingKey] = done }
    }
}
