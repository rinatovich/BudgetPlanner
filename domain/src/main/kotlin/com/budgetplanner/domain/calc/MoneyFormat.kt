package com.budgetplanner.domain.calc

import com.budgetplanner.domain.model.Currency
import java.util.Locale
import kotlin.math.abs

object MoneyFormat {
    private const val NBSP = '\u00A0'
    private const val MINUS = '\u2212'

    /** 40000000 → «40 000 000» (неразрывные пробелы). */
    fun grouped(value: Long): String {
        val digits = abs(value).toString()
        val sb = StringBuilder()
        digits.forEachIndexed { i, c ->
            if (i > 0 && (digits.length - i) % 3 == 0) sb.append(NBSP)
            sb.append(c)
        }
        return sb.toString()
    }

    /** «− 40 000 000 сум». Знак: минус для отрицательных, плюс — только если [showPlus]. */
    fun full(value: Long, currency: Currency, showPlus: Boolean = false, withCurrency: Boolean = true): String {
        val sign = when {
            value < 0 -> "$MINUS$NBSP"
            showPlus && value > 0 -> "+$NBSP"
            else -> ""
        }
        val suffix = if (withCurrency) "$NBSP${currency.symbol}" else ""
        return sign + grouped(value) + suffix
    }

    /** Компактная запись для графиков: 45M, 1.5M, 180K. */
    fun compact(value: Long): String {
        val a = abs(value).toDouble()
        val (number, suffix) = when {
            a >= 999_500_000 -> a / 1e9 to "B"
            a >= 999_500 -> a / 1e6 to "M"
            a >= 1_000 -> a / 1e3 to "K"
            else -> a to ""
        }
        val text = if (suffix.isEmpty() || number >= 100) {
            String.format(Locale.ROOT, "%.0f", number)
        } else {
            String.format(Locale.ROOT, "%.1f", number).removeSuffix(".0")
        }
        return (if (value < 0) "$MINUS" else "") + text + suffix
    }
}
