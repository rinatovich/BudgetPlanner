package com.budgetplanner.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import kotlinx.coroutines.delay

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

@Composable
fun AmountField(
    digits: String,
    onDigitsChange: (String) -> Unit,
    currencySymbol: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.headlineMedium,
    isError: Boolean = false,
    errorText: String? = null,
    autoFocus: Boolean = false,
    placeholder: String = "0",
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {},
) {
    val focusRequester = remember { FocusRequester() }
    if (autoFocus) {
        LaunchedEffect(Unit) {
            delay(250)
            runCatching { focusRequester.requestFocus() }
        }
    }
    TextField(
        value = digits,
        onValueChange = { onDigitsChange(sanitize(it)) },
        modifier = modifier.fillMaxWidth().focusRequester(focusRequester),
        textStyle = textStyle,
        singleLine = true,
        isError = isError,
        placeholder = { Text(placeholder, style = textStyle) },
        suffix = { Text(currencySymbol, style = textStyle, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        supportingText = errorText?.let { message -> { Text(message) } },
        visualTransformation = ThousandsTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = imeAction),
        keyboardActions = KeyboardActions(onDone = { onImeAction() }, onNext = { onImeAction() }),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
    )
}
