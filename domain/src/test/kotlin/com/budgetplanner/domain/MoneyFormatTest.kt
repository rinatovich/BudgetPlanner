package com.budgetplanner.domain

import com.budgetplanner.domain.calc.MoneyFormat
import com.budgetplanner.domain.model.Currency
import kotlin.test.Test
import kotlin.test.assertEquals

class MoneyFormatTest {
    private val nb = "\u00A0"

    @Test fun groupsThousands() {
        assertEquals("40${nb}000${nb}000", MoneyFormat.grouped(40_000_000))
        assertEquals("999", MoneyFormat.grouped(999))
        assertEquals("1${nb}000", MoneyFormat.grouped(1_000))
        assertEquals("0", MoneyFormat.grouped(0))
    }

    @Test fun fullFormatWithSigns() {
        assertEquals("40${nb}000${nb}сум", MoneyFormat.full(40_000, Currency.UZS))
        assertEquals("\u2212${nb}2${nb}500${nb}000${nb}сум", MoneyFormat.full(-2_500_000, Currency.UZS))
        assertEquals("+${nb}500${nb}сум", MoneyFormat.full(500, Currency.UZS, showPlus = true))
    }

    @Test fun compactFormat() {
        assertEquals("45M", MoneyFormat.compact(45_000_000))
        assertEquals("1.5M", MoneyFormat.compact(1_500_000))
        assertEquals("180K", MoneyFormat.compact(180_000))
        assertEquals("35K", MoneyFormat.compact(35_000))
        assertEquals("500", MoneyFormat.compact(500))
        assertEquals("\u22122.5M", MoneyFormat.compact(-2_500_000))
        assertEquals("1M", MoneyFormat.compact(999_600))
        assertEquals("1.2B", MoneyFormat.compact(1_200_000_000))
    }
}
