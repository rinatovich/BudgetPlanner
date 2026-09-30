package com.budgetplanner.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.budgetplanner.app.presentation.HomeUiState
import com.budgetplanner.app.ui.charts.BalanceLineChart
import com.budgetplanner.app.ui.charts.BarRow
import com.budgetplanner.app.ui.charts.DonutChart
import com.budgetplanner.app.ui.charts.DonutSlice
import com.budgetplanner.app.ui.charts.SplitBar
import com.budgetplanner.app.ui.components.AppBottomSheet
import com.budgetplanner.app.ui.components.AppCard
import com.budgetplanner.app.ui.components.CategoryAvatar
import com.budgetplanner.app.ui.components.EmptyState
import com.budgetplanner.app.ui.components.MonthSwitcher
import com.budgetplanner.app.ui.components.SecondaryText
import com.budgetplanner.app.ui.components.SectionTitle
import com.budgetplanner.app.ui.components.animatedLong
import com.budgetplanner.app.ui.components.formatMoney
import com.budgetplanner.app.ui.theme.BudgetTheme
import com.budgetplanner.app.ui.theme.MoneyStyles
import com.budgetplanner.domain.calc.CategoryTotal
import com.budgetplanner.domain.calc.MoneyFormat
import com.budgetplanner.domain.calc.MonthSummary
import com.budgetplanner.domain.model.Currency
import com.budgetplanner.domain.model.EntryType
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    state: HomeUiState?,
    month: YearMonth,
    padding: PaddingValues,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onReset: () -> Unit,
    onAddPlan: (EntryType) -> Unit,
) {
    var detail by remember { mutableStateOf<CategoryTotal?>(null) }

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = padding.calculateTopPadding() + 4.dp,
            bottom = padding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { MonthSwitcher(month, onPrevious, onNext, onReset) }

        if (state == null) {
            item {
                Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else if (!state.summary.hasData) {
            item {
                EmptyState(
                    icon = Icons.Rounded.AccountBalanceWallet,
                    title = "Пока нет данных за этот месяц",
                    subtitle = "Добавьте доход и обязательные расходы — и сразу увидите, сколько денег останется свободными.",
                ) {
                    Button(onClick = { onAddPlan(EntryType.INCOME) }) { Text("Добавить доход") }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { onAddPlan(EntryType.EXPENSE) }) { Text("Добавить расход") }
                }
            }
        } else {
            val s = state.summary
            item { HeroCard(s, state.currency) }
            if (s.isOverspent) item { OverspentBanner(s, state.currency) }
            item { ExpensesCard(s, state.currency) }
            item { MonthBarsCard(s, state.currency) }
            if (s.expenses > 0) item { DonutCard(s, state, onCategoryClick = { detail = it }) }
            item { BalanceCard(s, state.currency) }
        }
    }

    val selected = detail
    val current = state
    if (selected != null && current != null) {
        CategoryDetailSheet(
            total = selected,
            name = current.categories[selected.categoryId]?.name ?: "Без категории",
            iconKey = current.categories[selected.categoryId]?.icon ?: "other",
            totalExpenses = current.summary.expenses,
            currency = current.currency,
            onDismiss = { detail = null },
        )
    }
}

@Composable
private fun HeroCard(s: MonthSummary, currency: Currency) {
    val free = animatedLong(s.free)
    val extra = BudgetTheme.extra
    AppCard {
        SecondaryText(
            when {
                s.remainingDays == 0 -> "Итог месяца"
                s.isOverspent -> "Не хватает в этом месяце"
                else -> "Останется в этом месяце"
            },
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = formatMoney(free, currency, withCurrency = false),
                style = MoneyStyles.hero,
                color = if (s.isOverspent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Text(
                text = "\u00A0" + currency.symbol,
                style = MoneyStyles.medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }
        val weekly = s.weeklyFree
        if (weekly != null && !s.isOverspent) {
            Spacer(Modifier.height(12.dp))
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                Text(
                    text = "≈ ${formatMoney(weekly, currency)} в неделю",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.ArrowUpward, null, tint = extra.income, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    SecondaryText("Доходы")
                }
                Text(
                    formatMoney(s.income, currency, showPlus = true),
                    style = MoneyStyles.medium,
                    color = extra.income,
                    maxLines = 1,
                )
            }
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.ArrowDownward, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    SecondaryText("Расходы")
                }
                Text(
                    formatMoney(-s.expenses, currency),
                    style = MoneyStyles.medium,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun OverspentBanner(s: MonthSummary, currency: Currency) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.errorContainer) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Rounded.Warning, null, tint = MaterialTheme.colorScheme.onErrorContainer)
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Расходы превышают доходы на ${formatMoney(-s.free, currency)}. " +
                    "Попробуйте сократить необязательные траты или добавить доход.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}

@Composable
private fun ExpensesCard(s: MonthSummary, currency: Currency) {
    val extra = BudgetTheme.extra
    AppCard {
        SectionTitle("Обязательные и необязательные")
        Spacer(Modifier.height(16.dp))
        SplitBar(s.essentialExpenses, s.nonEssentialExpenses, extra.essential, extra.nonEssential)
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth()) {
            LegendAmount("Обязательные", s.essentialExpenses, currency, extra.essential, Modifier.weight(1f))
            LegendAmount("Необязательные", s.nonEssentialExpenses, currency, extra.nonEssential, Modifier.weight(1f))
        }
        val share = s.essentialShareOfIncomePercent
        if (share != null) {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Rounded.Info, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                SecondaryText("Обязательные расходы составляют $share% дохода.")
            }
        }
        if (s.actualExpenses > 0) {
            Spacer(Modifier.height(8.dp))
            SecondaryText("Уже потрачено по факту: ${formatMoney(s.actualExpenses, currency)}")
        }
    }
}

@Composable
private fun LegendAmount(label: String, value: Long, currency: Currency, color: Color, modifier: Modifier = Modifier) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(6.dp))
            SecondaryText(label)
        }
        Text(formatMoney(value, currency), style = MaterialTheme.typography.titleMedium, maxLines = 1)
    }
}

@Composable
private fun MonthBarsCard(s: MonthSummary, currency: Currency) {
    val extra = BudgetTheme.extra
    val maxValue = maxOf(s.income, s.expenses, kotlin.math.abs(s.free), 1L)
    AppCard {
        SectionTitle("Ваш месяц")
        Spacer(Modifier.height(16.dp))
        BarRow("Доходы", s.income, maxValue, extra.income, MoneyFormat.compact(s.income))
        Spacer(Modifier.height(12.dp))
        BarRow("Расходы", s.expenses, maxValue, extra.nonEssential, MoneyFormat.compact(s.expenses))
        Spacer(Modifier.height(12.dp))
        BarRow(
            "Свободно", s.free, maxValue,
            if (s.isOverspent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            MoneyFormat.compact(s.free),
        )
    }
}

private data class LegendItem(val name: String, val value: Long, val color: Color, val total: CategoryTotal?)

@Composable
private fun DonutCard(s: MonthSummary, state: HomeUiState, onCategoryClick: (CategoryTotal) -> Unit) {
    val palette = BudgetTheme.extra.chart
    val top = s.expenseCategories.take(5)
    val rest = s.expenseCategories.drop(5)
    val items = top.mapIndexed { i, t ->
        LegendItem(state.categories[t.categoryId]?.name ?: "Без категории", t.projected, palette[i % palette.size], t)
    } + (if (rest.isNotEmpty()) {
        listOf(LegendItem("Остальное", rest.sumOf { it.projected }, palette[5 % palette.size], null))
    } else {
        emptyList()
    })
    val total = s.expenses.coerceAtLeast(1)

    AppCard {
        SectionTitle("Куда уходят деньги")
        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            DonutChart(
                slices = items.map { DonutSlice(it.value, it.color) },
                modifier = Modifier.width(210.dp),
                center = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        SecondaryText("Расходы")
                        Text(MoneyFormat.compact(s.expenses), style = MoneyStyles.large)
                    }
                },
            )
        }
        Spacer(Modifier.height(16.dp))
        items.forEach { item ->
            val percent = (item.value * 100.0 / total).roundToInt()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (item.total != null) {
                            Modifier.clickable(onClickLabel = "Подробнее") { onCategoryClick(item.total) }
                        } else {
                            Modifier
                        },
                    )
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(12.dp).clip(CircleShape).background(item.color))
                Spacer(Modifier.width(12.dp))
                Text(item.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f), maxLines = 1)
                SecondaryText("$percent%")
                Spacer(Modifier.width(12.dp))
                Text(
                    MoneyFormat.compact(item.value),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.width(56.dp),
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

@Composable
private fun BalanceCard(s: MonthSummary, currency: Currency) {
    val today = LocalDate.now()
    val todayDay = if (YearMonth.from(today) == s.month) today.dayOfMonth else null
    AppCard {
        SectionTitle("Баланс по дням")
        SecondaryText("Изменение остатка с начала месяца")
        Spacer(Modifier.height(16.dp))
        BalanceLineChart(points = s.dailyBalance, lowest = s.lowestPoint, todayDay = todayDay)
        val low = s.lowestPoint
        if (low != null) {
            Spacer(Modifier.height(12.dp))
            SecondaryText("Минимум — ${low.day} числа: ${formatMoney(low.balance, currency)}")
        }
    }
}

@Composable
private fun CategoryDetailSheet(
    total: CategoryTotal,
    name: String,
    iconKey: String,
    totalExpenses: Long,
    currency: Currency,
    onDismiss: () -> Unit,
) {
    AppBottomSheet(onDismiss = onDismiss) { _ ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            CategoryAvatar(iconKey)
            Spacer(Modifier.width(12.dp))
            Text(name, style = MaterialTheme.typography.titleLarge)
        }
        Spacer(Modifier.height(20.dp))
        DetailRow("В плане на месяц", formatMoney(total.planned, currency))
        DetailRow("Потрачено по факту", formatMoney(total.actual, currency))
        DetailRow("Осталось в плане", formatMoney(total.leftInPlan, currency))
        HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
        DetailRow("Итого по прогнозу", formatMoney(total.projected, currency), emphasized = true)
        if (totalExpenses > 0) {
            val percent = (total.projected * 100.0 / totalExpenses).roundToInt()
            DetailRow("Доля всех расходов", "$percent%")
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, emphasized: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        if (emphasized) Text(label, style = MaterialTheme.typography.titleMedium) else SecondaryText(label)
        Text(
            value,
            style = if (emphasized) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
        )
    }
}
