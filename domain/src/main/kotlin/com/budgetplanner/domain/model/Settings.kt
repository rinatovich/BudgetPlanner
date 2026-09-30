package com.budgetplanner.domain.model

import java.time.DayOfWeek

/** Валюта. В первой версии используется одна валюта без конвертации. */
enum class Currency(val code: String, val symbol: String) {
    UZS("UZS", "сум"),
    USD("USD", "$"),
    EUR("EUR", "€"),
    RUB("RUB", "₽"),
}

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val currency: Currency = Currency.UZS,
    val firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val onboardingDone: Boolean = false,
)
