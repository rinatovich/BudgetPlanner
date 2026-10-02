@file:OptIn(ExperimentalLayoutApi::class)

package com.budgetplanner.app.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.budgetplanner.app.presentation.OnboardingExpense
import com.budgetplanner.app.presentation.OnboardingIncome
import com.budgetplanner.app.ui.components.AmountField
import com.budgetplanner.app.ui.components.CategoryAvatar
import com.budgetplanner.app.ui.components.DayOfMonthPicker
import com.budgetplanner.app.ui.components.GroupCard
import com.budgetplanner.app.ui.components.InlineAmountField
import com.budgetplanner.app.ui.components.IosEasing
import com.budgetplanner.app.ui.components.PickerPill
import com.budgetplanner.app.ui.components.PrimaryButton
import com.budgetplanner.app.ui.components.RowDivider
import com.budgetplanner.app.ui.components.SegmentedChoice
import com.budgetplanner.app.ui.components.TextAction
import com.budgetplanner.app.ui.components.WeekdayPicker
import com.budgetplanner.app.ui.components.parseAmount
import com.budgetplanner.app.ui.theme.AppText
import com.budgetplanner.app.ui.theme.BudgetTheme
import com.budgetplanner.domain.model.Currency
import com.budgetplanner.domain.model.Frequency
import java.time.DayOfWeek

private val incomeTitles = listOf("Зарплата", "Подработка", "Фриланс")
private val expenseNames = listOf("Жильё", "Кредиты", "Продукты", "Транспорт", "Другое")
private val expenseIcons = mapOf(
    "Жильё" to "home", "Кредиты" to "credit", "Продукты" to "cart", "Транспорт" to "transport", "Другое" to "other",
)

@Composable
fun OnboardingScreen(
    currency: Currency,
    onFinish: (OnboardingIncome?, List<OnboardingExpense>) -> Unit,
    onSkip: () -> Unit,
) {
    val c = BudgetTheme.colors
    var step by remember { mutableIntStateOf(0) }

    var incomeTitle by remember { mutableStateOf(incomeTitles.first()) }
    var incomeDigits by remember { mutableStateOf("") }
    var incomeFrequency by remember { mutableStateOf(Frequency.MONTHLY) }
    var incomeDayOfMonth by remember { mutableIntStateOf(5) }
    var incomeDayOfWeek by remember { mutableStateOf(DayOfWeek.FRIDAY) }
    val expenseDigits = remember { mutableStateMapOf<String, String>() }

    Box(Modifier.fillMaxSize().background(c.background)) {
        Column(Modifier.fillMaxSize().systemBarsPadding().imePadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.End) {
                TextAction("Пропустить", onClick = onSkip, color = c.blue)
            }

            AnimatedContent(
                targetState = step,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                transitionSpec = {
                    val dir = if (targetState > initialState) 1 else -1
                    (slideInHorizontally(tween(420, easing = IosEasing)) { it * dir / 3 } + fadeIn(tween(300))) togetherWith
                        (slideOutHorizontally(tween(420, easing = IosEasing)) { -it * dir / 3 } + fadeOut(tween(160)))
                },
                label = "onboarding-step",
            ) { current ->
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
                ) {
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

            Column(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 12.dp, top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PageDots(count = 3, current = step)
                Spacer(Modifier.height(16.dp))
                PrimaryButton(
                    text = when (step) {
                        0 -> "Начать"
                        1 -> "Далее"
                        else -> "Готово"
                    },
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
                )
                if (step > 0) {
                    TextAction("Назад", onClick = { step -= 1 })
                } else {
                    Spacer(Modifier.height(44.dp))
                }
            }
        }
    }
}

/** Точки страниц: активная растягивается в «капсулу». */
@Composable
private fun PageDots(count: Int, current: Int) {
    val c = BudgetTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(count) { i ->
            val width by animateDpAsState(
                targetValue = if (i == current) 20.dp else 7.dp,
                animationSpec = spring(dampingRatio = 0.75f, stiffness = 500f),
                label = "dot",
            )
            Box(
                Modifier.size(width = width, height = 7.dp).clip(CircleShape)
                    .background(if (i == current) c.blue else c.tertiaryLabel),
            )
        }
    }
}

@Composable
private fun WelcomeStep() {
    val c = BudgetTheme.colors
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(32.dp))
        Box(
            Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF5AC8FA), c.blue))),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.AccountBalanceWallet, contentDescription = null, tint = Color.White, modifier = Modifier.size(54.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text("Бюджет", style = AppText.largeTitle, color = c.label)
        Spacer(Modifier.height(8.dp))
        Text(
            "Спланируйте месяц заранее и узнайте, сколько денег останется свободными — в месяц и в неделю.",
            style = AppText.body,
            color = c.secondaryLabel,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(36.dp))
        Feature(Icons.Rounded.CalendarMonth, c.blue, "По календарю", "Регулярные доходы и расходы в нужные даты")
        Spacer(Modifier.height(22.dp))
        Feature(Icons.Rounded.CheckCircle, c.green, "Точный расчёт", "Учитывает 4 или 5 суббот и 28–31 день")
        Spacer(Modifier.height(22.dp))
        Feature(Icons.Rounded.Lock, c.orange, "Только у вас", "Работает без интернета, данные остаются на телефоне")
    }
}

@Composable
private fun Feature(icon: ImageVector, tint: Color, title: String, text: String) {
    val c = BudgetTheme.colors
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(tint),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, style = AppText.headline, color = c.label)
            Text(text, style = AppText.subhead, color = c.secondaryLabel)
        }
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
    val c = BudgetTheme.colors
    Column(Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(8.dp))
        Text("Ваш основной доход", style = AppText.title1, color = c.label)
        Spacer(Modifier.height(6.dp))
        Text("Его можно изменить позже в плане.", style = AppText.subhead, color = c.secondaryLabel)
        Spacer(Modifier.height(20.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            incomeTitles.forEach { name -> PickerPill(name, selected = title == name, onClick = { onTitle(name) }) }
        }
        Spacer(Modifier.height(28.dp))
        AmountField(digits = digits, onDigitsChange = onDigits, currencySymbol = currency.symbol)
        Spacer(Modifier.height(28.dp))
        SegmentedChoice(
            options = listOf("Каждый месяц", "Каждую неделю"),
            selectedIndex = if (frequency == Frequency.MONTHLY) 0 else 1,
            onSelect = { onFrequency(if (it == 0) Frequency.MONTHLY else Frequency.WEEKLY) },
        )
        Spacer(Modifier.height(16.dp))
        GroupCard(Modifier.animateContentSize()) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    if (frequency == Frequency.MONTHLY) "Какого числа" else "В какой день недели",
                    style = AppText.footnote,
                    color = c.secondaryLabel,
                )
                Spacer(Modifier.height(10.dp))
                if (frequency == Frequency.MONTHLY) {
                    DayOfMonthPicker(dayOfMonth, onSelect = onDayOfMonth)
                } else {
                    WeekdayPicker(dayOfWeek, DayOfWeek.MONDAY, onSelect = onDayOfWeek)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ExpensesStep(currency: Currency, values: MutableMap<String, String>) {
    val c = BudgetTheme.colors
    Column(Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(8.dp))
        Text("Основные расходы", style = AppText.title1, color = c.label)
        Spacer(Modifier.height(6.dp))
        Text("Укажите то, что платите регулярно. Пустые поля можно пропустить.", style = AppText.subhead, color = c.secondaryLabel)
        Spacer(Modifier.height(20.dp))
        GroupCard {
            expenseNames.forEachIndexed { index, name ->
                val suffix = if (name == "Продукты") "в неделю" else "в месяц"
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CategoryAvatar(expenseIcons[name] ?: "other")
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.width(112.dp)) {
                        Text(name, style = AppText.body, color = c.label, maxLines = 1)
                        Text(suffix, style = AppText.caption1, color = c.secondaryLabel)
                    }
                    InlineAmountField(
                        digits = values[name] ?: "",
                        onDigitsChange = { values[name] = it },
                        currencySymbol = currency.symbol,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (index < expenseNames.lastIndex) RowDivider(start = 62.dp)
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
