@file:OptIn(ExperimentalFoundationApi::class)

package com.budgetplanner.app.ui.plan

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.budgetplanner.app.presentation.PlanRow
import com.budgetplanner.app.presentation.PlanUiState
import com.budgetplanner.app.ui.components.CategoryAvatar
import com.budgetplanner.app.ui.components.EmptyState
import com.budgetplanner.app.ui.components.LargeTitleScreen
import com.budgetplanner.app.ui.components.LoadingRow
import com.budgetplanner.app.ui.components.MonthSwitcher
import com.budgetplanner.app.ui.components.PrimaryButton
import com.budgetplanner.app.ui.components.RowDivider
import com.budgetplanner.app.ui.components.SectionHeader
import com.budgetplanner.app.ui.components.SwipeToDeleteBox
import com.budgetplanner.app.ui.components.TextAction
import com.budgetplanner.app.ui.components.TodayChip
import com.budgetplanner.app.ui.components.formatMoney
import com.budgetplanner.app.ui.components.frequencyUnit
import com.budgetplanner.app.ui.components.groupShape
import com.budgetplanner.app.ui.components.rowClickable
import com.budgetplanner.app.ui.components.scheduleText
import com.budgetplanner.app.ui.theme.AppText
import com.budgetplanner.app.ui.theme.BudgetTheme
import com.budgetplanner.app.ui.theme.MoneyStyles
import com.budgetplanner.domain.calc.MoneyFormat
import com.budgetplanner.domain.model.Currency
import com.budgetplanner.domain.model.EntryType
import com.budgetplanner.domain.model.Frequency
import com.budgetplanner.domain.model.PlanItem
import java.time.YearMonth

@Composable
fun PlanScreen(
    state: PlanUiState?,
    month: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onReset: () -> Unit,
    onEdit: (PlanItem) -> Unit,
    onAdd: (EntryType) -> Unit,
    onDelete: (PlanItem) -> Unit,
) {
    LargeTitleScreen(
        title = "План",
        titleAccessory = { TodayChip(visible = month != YearMonth.now(), onClick = onReset) },
    ) {
        item(key = "month") { MonthSwitcher(month, onPrevious, onNext, onReset) }

        if (state == null) {
            item(key = "loading") { LoadingRow() }
        } else if (state.incomes.isEmpty() && state.expenses.isEmpty()) {
            item(key = "empty") {
                EmptyState(
                    icon = Icons.Rounded.CalendarMonth,
                    title = "План пока пуст",
                    subtitle = "Добавьте регулярные доходы и расходы: зарплату, аренду, кредиты, еженедельные покупки.",
                ) {
                    PrimaryButton("Добавить доход", onClick = { onAdd(EntryType.INCOME) })
                    Spacer(Modifier.height(4.dp))
                    TextAction("Добавить расход", onClick = { onAdd(EntryType.EXPENSE) })
                }
            }
        } else {
            planSection("Доходы", EntryType.INCOME, state.incomes, state.currency, "Добавить доход", onEdit, onAdd, onDelete)
            planSection("Расходы", EntryType.EXPENSE, state.expenses, state.currency, "Добавить расход", onEdit, onAdd, onDelete)
        }
    }
}

private fun LazyListScope.planSection(
    title: String,
    type: EntryType,
    rows: List<PlanRow>,
    currency: Currency,
    addLabel: String,
    onEdit: (PlanItem) -> Unit,
    onAdd: (EntryType) -> Unit,
    onDelete: (PlanItem) -> Unit,
) {
    val activeTotal = rows.filter { it.item.isActive }.sumOf { it.monthTotal }
    val count = rows.size + 1

    item(key = "header-$title") {
        SectionHeader(title, trailing = "в месяце " + formatMoney(activeTotal, currency, showPlus = type == EntryType.INCOME))
    }
    itemsIndexed(rows, key = { _, row -> "plan-${row.item.id}" }) { index, row ->
        SwipeToDeleteBox(
            shape = groupShape(index, count),
            onDelete = { onDelete(row.item) },
            modifier = Modifier.animateItemPlacement(),
        ) {
            PlanRowContent(row, currency, onClick = { onEdit(row.item) })
        }
    }
    item(key = "add-$title") {
        AddRow(addLabel, groupShape(rows.size, count), { onAdd(type) }, Modifier.animateItemPlacement())
    }
}

@Composable
private fun PlanRowContent(row: PlanRow, currency: Currency, onClick: () -> Unit) {
    val c = BudgetTheme.colors
    val item = row.item
    val isIncome = item.type == EntryType.INCOME
    val multiple = item.frequency == Frequency.WEEKLY || item.frequency == Frequency.DAILY
    val subtitle = buildString {
        if (!item.isActive) append("Отключено · ")
        append(scheduleText(item))
        if (multiple) append(" · ${row.occurrences}× = ${MoneyFormat.compact(row.monthTotal)}")
    }
    Column(Modifier.alpha(if (item.isActive) 1f else 0.5f)) {
        Row(
            Modifier.fillMaxWidth().rowClickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryAvatar(row.category?.icon ?: "other")
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.title, style = AppText.body, color = c.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, style = AppText.footnote, color = c.secondaryLabel, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatMoney(item.amount, currency, showPlus = isIncome, withCurrency = false),
                    style = MoneyStyles.row,
                    color = if (isIncome) c.green else c.label,
                    maxLines = 1,
                )
                val unit = frequencyUnit(item.frequency)
                Text(
                    text = if (unit.isEmpty()) currency.symbol else "${currency.symbol} $unit",
                    style = AppText.caption1,
                    color = c.secondaryLabel,
                )
            }
        }
        RowDivider(start = 62.dp)
    }
}

@Composable
private fun AddRow(label: String, shape: RoundedCornerShape, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = BudgetTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .rowClickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Rounded.Add,
            contentDescription = null,
            tint = c.blue,
            modifier = Modifier.size(34.dp).clip(RoundedCornerShape(9.dp)).background(c.blue.copy(alpha = 0.12f)).padding(6.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(label, style = AppText.body, color = c.blue)
    }
}
