package com.budgetplanner.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budgetplanner.app.ui.theme.AppText
import com.budgetplanner.app.ui.theme.BudgetTheme
import com.budgetplanner.app.ui.theme.MoneyStyles
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private const val NBSP = '\u00A0'

/** Показывает «40000000» как «40 000 000», не меняя сам ввод. */
object ThousandsTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val n = digits.length
        val out = StringBuilder()
        val o2t = IntArray(n + 1)
        val t2o = ArrayList<Int>()

        for (i in 0 until n) {
            if (i > 0 && (n - i) % 3 == 0) {
                out.append(NBSP)
                t2o.add(i)
            }
            o2t[i] = out.length
            out.append(digits[i])
            t2o.add(i)
        }
        o2t[n] = out.length
        t2o.add(n)

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                o2t[offset.coerceIn(0, n)]

            override fun transformedToOriginal(offset: Int): Int =
                t2o[offset.coerceIn(0, t2o.size - 1)]
        }
        return TransformedText(AnnotatedString(out.toString()), mapping)
    }
}

/** Разбирает сумму из строки цифр; пустая строка → 0. */
fun parseAmount(digits: String): Long = digits.toLongOrNull() ?: 0L

private fun sanitize(input: String): String =
    input.filter { it in '0'..'9' }.trimStart('0').take(12)

/**
 * Крупное поле суммы по центру: цифры сами уменьшаются, когда число длинное,
 * а при ошибке поле «трясётся» и даёт тактильный отклик.
 */
@Composable
fun AmountField(
    digits: String,
    onDigitsChange: (String) -> Unit,
    currencySymbol: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorText: String? = null,
    autoFocus: Boolean = false,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {},
) {
    val c = BudgetTheme.colors
    val focusRequester = remember { FocusRequester() }
    val thud = rememberThud()
    val shake = remember { Animatable(0f) }

    if (autoFocus) {
        LaunchedEffect(Unit) {
            delay(250)
            runCatching { focusRequester.requestFocus() }
        }
    }
    LaunchedEffect(isError) {
        if (isError) {
            thud()
            repeat(3) {
                shake.animateTo(12f, tween(45))
                shake.animateTo(-12f, tween(45))
            }
            shake.animateTo(0f, tween(45))
        }
    }

    val size = when {
        digits.length > 10 -> 32.sp
        digits.length > 8 -> 40.sp
        else -> 48.sp
    }
    val style: TextStyle = MoneyStyles.input.copy(
        fontSize = size,
        lineHeight = size * 1.17f,
        color = if (isError) c.red else c.label,
    )

    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            Modifier.offset { IntOffset(shake.value.roundToInt(), 0) },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Box(Modifier.width(IntrinsicSize.Min)) {
                BasicTextField(
                    value = digits,
                    onValueChange = { onDigitsChange(sanitize(it)) },
                    modifier = Modifier.focusRequester(focusRequester),
                    textStyle = style,
                    singleLine = true,
                    cursorBrush = SolidColor(c.blue),
                    visualTransformation = ThousandsTransformation,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = imeAction),
                    keyboardActions = KeyboardActions(onDone = { onImeAction() }, onNext = { onImeAction() }),
                    decorationBox = { inner ->
                        Box {
                            if (digits.isEmpty()) Text("0", style = style, color = c.tertiaryLabel)
                            inner()
                        }
                    },
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(currencySymbol, style = MoneyStyles.unit, color = c.secondaryLabel)
        }
        if (errorText != null) {
            Text(errorText, style = AppText.footnote, color = c.red, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

/** Сумма в строке формы: подпись слева, число справа. */
@Composable
fun InlineAmountField(
    digits: String,
    onDigitsChange: (String) -> Unit,
    currencySymbol: String,
    modifier: Modifier = Modifier,
    placeholder: String = "0",
) {
    val c = BudgetTheme.colors
    val style = AppText.body.copy(color = c.label, textAlign = TextAlign.End, fontFeatureSettings = "tnum")
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
        BasicTextField(
            value = digits,
            onValueChange = { onDigitsChange(sanitize(it)) },
            modifier = Modifier.weight(1f),
            textStyle = style,
            singleLine = true,
            cursorBrush = SolidColor(c.blue),
            visualTransformation = ThousandsTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            decorationBox = { inner ->
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    if (digits.isEmpty()) Text(placeholder, style = style, color = c.tertiaryLabel)
                    inner()
                }
            },
        )
        Spacer(Modifier.width(6.dp))
        Text(currencySymbol, style = AppText.body, color = c.secondaryLabel)
    }
}

/** Текстовое поле в строке формы: без рамки, текст выровнен вправо. */
@Composable
fun InlineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
) {
    val c = BudgetTheme.colors
    val style = AppText.body.copy(color = c.label, textAlign = TextAlign.End)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = style,
        singleLine = singleLine,
        cursorBrush = SolidColor(c.blue),
        decorationBox = { inner ->
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                if (value.isEmpty()) Text(placeholder, style = style, color = c.tertiaryLabel)
                inner()
            }
        },
    )
}
