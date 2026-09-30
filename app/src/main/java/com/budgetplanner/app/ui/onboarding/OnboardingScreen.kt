@file:OptIn(ExperimentalLayoutApi::class)

package com.budgetplanner.app.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.budgetplanner.app.presentation.OnboardingExpense
import com.budgetplanner.app.presentation.OnboardingIncome
import com.budgetplanner.app.ui.components.AmountField
import com.budgetplanner.app.ui.components.DayOfMonthPicker
import com.budgetplanner.app.ui.components.SecondaryText
import com.budgetplanner.app.ui.components.WeekdayPicker
import com.budgetplanner.app.ui.components.parseAmount
import com.budgetplanner.app.ui.components.ThousandsTransformation
import com.budgetplanner.domain.model.Currency
import com.budgetplanner.domain.model.Frequency
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import java.time.DayOfWeek

private val incomeTitles = listOf("Зарплата", "Подработка", "Фриланс")
private val expenseNames = listOf("Жильё", "Кредиты", "Продукты", "Транспорт", "Другое")

@Composable
fun OnboardingScreen(
    currency: Currency,
    onFinish: (OnboardingIncome?, List<OnboardingExpense>) -> Unit,
    onSkip: () -> Unit,
) {
    var step by remember { mutableIntStateOf(0) }

    var incomeTitle by remember { mutableStateOf(incomeTitles.first()) }
    var incomeDigits by remember { mutableStateOf("") }
    var incomeFrequency by remember { mutableStateOf(Frequency.MONTHLY) }
    var incomeDayOfMonth by remember { mutableIntStateOf(5) }
    var incomeDayOfWeek by remember { mutableStateOf(DayOfWeek.FRIDAY) }
    val expenseDigits = remember { mutableStateMapOf<String, String>() }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onSkip) { Text("Пропустить") }
            }
            Spacer(Modifier.height(8.dp))

            AnimatedContent(
                targetState = step,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "onboarding-step",
            ) { current ->
                Column(Modifier.fillMaxWidth()) {
                    when (current) {
                        0 -> WelcomeStep()
                        1 -> IncomeStep(
                            currency = currency,
                            title = incomeTitle,
                            onTitle = { incomeTitle = it },
                            digits = incomeDigits,
                            onDigits = { incomeDigits = it },
                            frequency = incomeFrequency,
                            onFrequency = { incomeFrequency = it },
                            dayOfMonth = incomeDayOfMonth,
                            onDayOfMonth = { incomeDayOfMonth = it },
                            dayOfWeek = incomeDayOfWeek,
                            onDayOfWeek = { incomeDayOfWeek = it },
                        )
                        else -> ExpensesStep(currency, expenseDigits)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    if (step < 2) {
                        step += 1
                    } else {
                        val amount = parseAmount(incomeDigits)
                        val income = if (amount > 0) {
                            OnboardingIncome(incomeTitle, amount, incomeFrequency, incomeDayOfMonth, incomeDayOfWeek)
                        } else {
                            null
                        }
                        val expenses = expenseNames.mapNotNull { name ->
                            val value = parseAmount(expenseDigits[name] ?: "")
                            if (value > 0) OnboardingExpense(name, value) else null
                        }
                        onFinish(income, expenses)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                Text(
                    when (step) {
                        0 -> "Начать"
                        1 -> "Далее"
                        else -> "Готово"
                    },
                )
            }
            Spacer(Modifier.height(8.dp))
            SecondaryText("Шаг ${step + 1} из 3", modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
private fun WelcomeStep() {
    Column {
        Text("Бюджет", style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(12.dp))
        Text(
            "Спланируйте месяц заранее и узнайте, сколько денег останется свободными — в месяц и в неделю.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(24.dp))
        listOf(
            "Регулярные доходы и расходы по календарю",
            "Точный расчёт: 4 или 5 суббот, 28–31 день",
            "Работает без интернета, данные только у вас",
        ).forEach {
            Text("•  $it", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(vertical = 4.dp))
        }
        Spacer(Modifier.height(16.dp))
        SecondaryText("Настройка займёт около минуты. Всё можно пропустить и добавить позже.")
    }
}

@Composable
private fun IncomeStep(
    currency: Currency,
    title: String,
    onTitle: (String) -> Unit,
    digits: String,
    onDigits: (String) -> Unit,
    frequency: Frequency,
    onFrequency: (Frequency) -> Unit,
    dayOfMonth: Int,
    onDayOfMonth: (Int) -> Unit,
    dayOfWeek: DayOfWeek,
    onDayOfWeek: (DayOfWeek) -> Unit,
) {
    Column {
        Text("Ваш основной доход", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            incomeTitles.forEach { name ->
                FilterChip(selected = title == name, onClick = { onTitle(name) }, label = { Text(name) })
            }
        }
        Spacer(Modifier.height(12.dp))
        Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant) {
            AmountField(digits = digits, onDigitsChange = onDigits, currencySymbol = currency.symbol)
        }
        Spacer(Modifier.height(16.dp))
        SecondaryText("Как часто вы получаете")
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = frequency == Frequency.MONTHLY,
                onClick = { onFrequency(Frequency.MONTHLY) },
                label = { Text("Каждый месяц") },
            )
            FilterChip(
                selected = frequency == Frequency.WEEKLY,
                onClick = { onFrequency(Frequency.WEEKLY) },
                label = { Text("Каждую неделю") },
            )
        }
        Spacer(Modifier.height(16.dp))
        if (frequency == Frequency.MONTHLY) {
            SecondaryText("Какого числа")
            Spacer(Modifier.height(8.dp))
            DayOfMonthPicker(dayOfMonth, onSelect = onDayOfMonth)
        } else {
            SecondaryText("В какой день недели")
            Spacer(Modifier.height(8.dp))
            WeekdayPicker(dayOfWeek, DayOfWeek.MONDAY, onSelect = onDayOfWeek)
        }
    }
}

@Composable
private fun ExpensesStep(currency: Currency, values: MutableMap<String, String>) {
    Column {
        Text("Основные расходы", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        SecondaryText("Укажите то, что платите регулярно. Пустые поля можно пропустить.")
        Spacer(Modifier.height(16.dp))
        expenseNames.forEach { name ->
            val suffix = if (name == "Продукты") "в неделю" else "в месяц"
            OutlinedTextField(
                value = values[name] ?: "",
                onValueChange = { input ->
                    values[name] = input.filter { it in '0'..'9' }.trimStart('0').take(12)
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                singleLine = true,
                label = { Text("$name · $suffix") },
                suffix = { Text(currency.symbol) },
                visualTransformation = ThousandsTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }
    }
}
