package com.budgetplanner.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budgetplanner.app.presentation.HomeUiState
import com.budgetplanner.app.ui.charts.AnimatedBar
import com.budgetplanner.app.ui.charts.BalanceLineChart
import com.budgetplanner.app.ui.components.AppBottomSheet
import com.budgetplanner.app.ui.components.CategoryAvatar
import com.budgetplanner.app.ui.components.EmptyState
import com.budgetplanner.app.ui.components.GroupCard
import com.budgetplanner.app.ui.components.LargeTitleScreen
import com.budgetplanner.app.ui.components.LoadingRow
import com.budgetplanner.app.ui.components.MonthSwitcher
import com.budgetplanner.app.ui.components.NavIconButton
import com.budgetplanner.app.ui.components.PrimaryButton
import com.budgetplanner.app.ui.components.RowDivider
import com.budgetplanner.app.ui.components.SectionFooter
import com.budgetplanner.app.ui.components.SheetHorizontal
import com.budgetplanner.app.ui.components.TextAction
import com.budgetplanner.app.ui.components.TodayChip
import com.budgetplanner.app.ui.components.animatedLong
import com.budgetplanner.app.ui.components.categoryColor
import com.budgetplanner.app.ui.components.dayLabel
import com.budgetplanner.app.ui.components.formatMoney
import com.budgetplanner.app.ui.components.rowClickable
import com.budgetplanner.app.ui.components.staggerIn
import com.budgetplanner.app.ui.theme.AppText
import com.budgetplanner.app.ui.theme.BudgetTheme
import com.budgetplanner.app.ui.theme.MoneyStyles
import com.budgetplanner.domain.calc.CategoryTotal
import com.budgetplanner.domain.calc.DailyBalance
import com.budgetplanner.domain.calc.MoneyFormat
import com.budgetplanner.domain.calc.MonthSummary
import com.budgetplanner.domain.model.Currency
import com.budgetplanner.domain.model.EntryType
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.roundToInt

private const val NBSP = "\u00A0"

@Composable
fun HomeScreen(
    state: HomeUiState?,
    month: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onReset: () -> Unit,
    onAddPlan: (EntryType) -> Unit,
    onOpenSettings: () -> Unit,
) {
    var detail by remember { mutableStateOf<CategoryTotal?>(null) }

    // Блоки выезжают каскадом при открытии экрана и при смене месяца.
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(month) {
        revealed = false
        delay(60)
        revealed = true
    }

    LargeTitleScreen(
        title = "Обзор",
        trailing = { NavIconButton(Icons.Rounded.Settings, "Настройки", onOpenSettings) },
        titleAccessory = { TodayChip(visible = month != YearMonth.now(), onClick = onReset) },
        contentSpacing = 20.dp,
    ) {
        item(key = "month") {
            MonthSwitcher(month, onPrevious, onNext, onReset, Modifier.staggerIn(revealed, 0))
        }

        if (state == null) {
            item(key = "loading") { LoadingRow() }
        } else if (!state.summary.hasData) {
            item(key = "empty") {
                EmptyState(
                    icon = Icons.Rounded.AccountBalanceWallet,
                    title = "Пока нет данных за этот месяц",
                    subtitle = "Добавьте доход и обязательные расходы — и сразу увидите, сколько денег останется свободными.",
                    modifier = Modifier.staggerIn(revealed, 1),
                ) {
                    PrimaryButton("Добавить доход", onClick = { onAddPlan(EntryType.INCOME) })
                    Spacer(Modifier.height(4.dp))
                    TextAction("Добавить расход", onClick = { onAddPlan(EntryType.EXPENSE) })
                }
            }
        } else {
            val s = state.summary
            item(key = "hero") {
                Column(Modifier.staggerIn(revealed, 1), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    HeroSection(s, state.currency)
                    SummaryCard(s, state.currency)
                }
            }
            item(key = "balance") { BalanceCard(s, state.currency, Modifier.staggerIn(revealed, 2)) }
            if (s.expenses > 0) {
                item(key = "categories") {
                    CategoriesCard(s, state, onCategoryClick = { detail = it }, modifier = Modifier.staggerIn(revealed, 3))
                }
            }
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

/** Главное число месяца — крупно, без карточки, прямо на фоне. */
@Composable
private fun HeroSection(s: MonthSummary, currency: Currency) {
    val c = BudgetTheme.colors
    val free = animatedLong(s.free)
    val amount = formatMoney(free, currency, withCurrency = false)
    val fontSize = when {
        amount.length > 13 -> 32.sp
        amount.length > 11 -> 38.sp
        else -> 46.sp
    }
    Column(Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
        Text(
            text = when {
                s.remainingDays == 0 -> "Итог месяца"
                s.isOverspent -> "Не хватает в этом месяце"
                else -> "Останется в этом месяце"
            },
            style = AppText.subhead,
            color = c.secondaryLabel,
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = amount,
                style = MoneyStyles.hero.copy(fontSize = fontSize, lineHeight = fontSize * 1.13f),
                color = if (s.isOverspent) c.red else c.label,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.weight(1f, fill = false),
            )
            Text(
                text = NBSP + currency.symbol,
                style = MoneyStyles.unit,
                color = c.secondaryLabel,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }
        val weekly = s.weeklyFree
        if (weekly != null && !s.isOverspent) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = "≈ ${formatMoney(weekly, currency)} в неделю",
                style = AppText.subhead.copy(fontWeight = FontWeight.SemiBold),
                color = c.blue,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(c.blue.copy(alpha = 0.12f))
                    .padding(horizontal = 14.dp, vertical = 7.dp),
            )
        }
        if (s.isOverspent) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Расходы больше доходов. Сократите необязательные траты или добавьте доход.",
                style = AppText.footnote,
                color = c.red,
            )
        }
    }
}

@Composable
private fun SummaryCard(s: MonthSummary, currency: Currency) {
    val c = BudgetTheme.colors
    GroupCard {
        Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            SummaryColumn(
                label = "Доходы",
                dot = c.green,
                value = formatMoney(s.income, currency, showPlus = true, withCurrency = false),
                valueColor = c.green,
                symbol = currency.symbol,
                note = null,
                modifier = Modifier.weight(1f),
            )
            Box(Modifier.width(0.5.dp).height(44.dp).background(c.separator))
            SummaryColumn(
                label = "Расходы",
                dot = c.secondaryLabel,
                value = formatMoney(-s.expenses, currency, withCurrency = false),
                valueColor = c.label,
                symbol = currency.symbol,
                note = if (s.actualExpenses > 0) "по факту ${MoneyFormat.compact(s.actualExpenses)}" else null,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SummaryColumn(
    label: String,
    dot: Color,
    value: String,
    valueColor: Color,
    symbol: String,
    note: String?,
    modifier: Modifier = Modifier,
) {
    val c = BudgetTheme.colors
    Column(modifier.padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(dot))
            Spacer(Modifier.width(6.dp))
            Text(label, style = AppText.footnote, color = c.secondaryLabel)
        }
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MoneyStyles.medium,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            Text(NBSP + symbol, style = AppText.caption1, color = c.secondaryLabel, modifier = Modifier.padding(bottom = 3.dp))
        }
        if (note != null) Text(note, style = AppText.caption1, color = c.secondaryLabel)
    }
}

@Composable
private fun BalanceCard(s: MonthSummary, currency: Currency, modifier: Modifier = Modifier) {
    val c = BudgetTheme.colors
    var scrub by remember { mutableStateOf<DailyBalance?>(null) }
    val today = LocalDate.now()
    val todayDay = if (YearMonth.from(today) == s.month) today.dayOfMonth else null
    val low = s.lowestPoint
    val picked = scrub

    GroupCard(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text("Баланс по дням", style = AppText.headline, color = c.label)
            Spacer(Modifier.height(2.dp))
            val readout = when {
                picked != null -> "${dayLabel(s.month.atDay(picked.day), today)} · ${formatMoney(picked.balance, currency)}"
                low != null -> "Минимум — ${low.day} числа: ${formatMoney(low.balance, currency)}"
                else -> "Изменение остатка с начала месяца"
            }
            val readoutColor = when {
                picked != null -> if (picked.balance < 0) c.red else c.label
                low != null && low.balance < 0 -> c.red
                else -> c.secondaryLabel
            }
            Text(readout, style = AppText.footnote, color = readoutColor, maxLines = 1)
            Spacer(Modifier.height(14.dp))
            BalanceLineChart(points = s.dailyBalance, lowest = low, todayDay = todayDay, onScrub = { scrub = it })
        }
    }
}

private data class CatRow(val name: String, val iconKey: String, val value: Long, val total: CategoryTotal?)

@Composable
private fun CategoriesCard(
    s: MonthSummary,
    state: HomeUiState,
    onCategoryClick: (CategoryTotal) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = BudgetTheme.colors
    val top = s.expenseCategories.take(5)
    val rest = s.expenseCategories.drop(5)
    val rows = top.map { t ->
        val cat = state.categories[t.categoryId]
        CatRow(cat?.name ?: "Без категории", cat?.icon ?: "other", t.projected, t)
    } + (if (rest.isNotEmpty()) listOf(CatRow("Остальное", "other", rest.sumOf { it.projected }, null)) else emptyList())
    val total = s.expenses.coerceAtLeast(1)
    val maxValue = rows.maxOfOrNull { it.value }?.coerceAtLeast(1) ?: 1L

    Column(modifier) {
        GroupCard {
            Row(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Куда уходят деньги", style = AppText.headline, color = c.label)
                Text(MoneyFormat.compact(s.expenses), style = AppText.subhead, color = c.secondaryLabel)
            }
            rows.forEachIndexed { index, row ->
                CategoryRow(row, percent = (row.value * 100.0 / total).roundToInt(), fraction = row.value.toFloat() / maxValue, index = index, onClick = row.total?.let { t -> { onCategoryClick(t) } })
                if (index < rows.lastIndex) RowDivider(start = 62.dp)
            }
        }
        val share = s.essentialShareOfIncomePercent
        if (share != null) SectionFooter("Обязательные расходы составляют $share% дохода.")
    }
}

@Composable
private fun CategoryRow(row: CatRow, percent: Int, fraction: Float, index: Int, onClick: (() -> Unit)?) {
    val c = BudgetTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.rowClickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryAvatar(row.iconKey)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(row.name, style = AppText.body, color = c.label, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                Text(MoneyFormat.compact(row.value), style = MoneyStyles.row, color = c.label, maxLines = 1)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedBar(fraction, categoryColor(row.iconKey), Modifier.weight(1f), delayMillis = 200 + index * 70)
                Text(
                    "$percent%",
                    style = AppText.caption1,
                    color = c.secondaryLabel,
                    modifier = Modifier.width(40.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                )
            }
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
    val c = BudgetTheme.colors
    AppBottomSheet(onDismiss = onDismiss) { _ ->
        Column(Modifier.padding(horizontal = SheetHorizontal)) {
            Row(Modifier.padding(top = 8.dp, bottom = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                CategoryAvatar(iconKey, size = 48.dp)
                Spacer(Modifier.width(14.dp))
                Text(name, style = AppText.title2, color = c.label)
            }
            GroupCard {
                DetailRow("В плане на месяц", formatMoney(total.planned, currency))
                RowDivider()
                DetailRow("Потрачено по факту", formatMoney(total.actual, currency))
                RowDivider()
                DetailRow("Осталось в плане", formatMoney(total.leftInPlan, currency))
            }
            Spacer(Modifier.height(16.dp))
            GroupCard {
                DetailRow("Итого по прогнозу", formatMoney(total.projected, currency), emphasized = true)
                if (totalExpenses > 0) {
                    RowDivider()
                    DetailRow("Доля всех расходов", "${(total.projected * 100.0 / totalExpenses).roundToInt()}%")
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, emphasized: Boolean = false) {
    val c = BudgetTheme.colors
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = AppText.body, color = c.label)
        Text(
            value,
            style = if (emphasized) MoneyStyles.row else AppText.body.copy(fontFeatureSettings = "tnum"),
            color = if (emphasized) c.label else c.secondaryLabel,
        )
    }
}
