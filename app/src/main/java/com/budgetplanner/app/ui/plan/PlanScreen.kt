package com.budgetplanner.app.ui.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.budgetplanner.app.presentation.PlanRow
import com.budgetplanner.app.presentation.PlanUiState
import com.budgetplanner.app.ui.components.CategoryAvatar
import com.budgetplanner.app.ui.components.ConfirmDialog
import com.budgetplanner.app.ui.components.EmptyState
import com.budgetplanner.app.ui.components.MonthSwitcher
import com.budgetplanner.app.ui.components.SecondaryText
import com.budgetplanner.app.ui.components.SwipeToDeleteBox
import com.budgetplanner.app.ui.components.formatMoney
import com.budgetplanner.app.ui.components.frequencyUnit
import com.budgetplanner.app.ui.components.scheduleText
import com.budgetplanner.app.ui.theme.BudgetTheme
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
    padding: PaddingValues,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onReset: () -> Unit,
    onEdit: (PlanItem) -> Unit,
    onAdd: (EntryType) -> Unit,
    onDelete: (Long) -> Unit,
) {
    var pendingDelete by remember { mutableStateOf<PlanItem?>(null) }

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = padding.calculateTopPadding() + 4.dp,
            bottom = padding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { MonthSwitcher(month, onPrevious, onNext, onReset) }

        if (state == null) {
            item {
                Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else {
            if (state.incomes.isEmpty() && state.expenses.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Rounded.CalendarMonth,
                        title = "План пока пуст",
                        subtitle = "Добавьте регулярные доходы и расходы: зарплату, аренду, кредиты, еженедельные покупки.",
                    )
                }
            }
            planSection(
                title = "Доходы",
                type = EntryType.INCOME,
                rows = state.incomes,
                currency = state.currency,
                addLabel = "Добавить доход",
                onEdit = onEdit,
                onAdd = onAdd,
                onRequestDelete = { pendingDelete = it },
            )
            planSection(
                title = "Расходы",
                type = EntryType.EXPENSE,
                rows = state.expenses,
                currency = state.currency,
                addLabel = "Добавить расход",
                onEdit = onEdit,
                onAdd = onAdd,
                onRequestDelete = { pendingDelete = it },
            )
        }
    }

    val target = pendingDelete
    if (target != null) {
        ConfirmDialog(
            title = "Удалить «${target.title}»?",
            text = "Операция исчезнет из плана во всех месяцах.",
            confirmLabel = "Удалить",
            onConfirm = {
                onDelete(target.id)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
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
    onRequestDelete: (PlanItem) -> Unit,
) {
    val activeTotal = rows.filter { it.item.isActive }.sumOf { it.monthTotal }
    item {
        Row(
            Modifier.fillMaxWidth().padding(top = 12.dp, start = 4.dp, end = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title.uppercase(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            SecondaryText("в месяце: " + formatMoney(activeTotal, currency))
        }
    }
    items(rows.size, key = { rows[it].item.id }) { index ->
        val row = rows[index]
        SwipeToDeleteBox(onRequestDelete = { onRequestDelete(row.item) }) {
            PlanRowCard(row, currency, onClick = { onEdit(row.item) })
        }
    }
    item {
        TextButton(onClick = { onAdd(type) }) {
            Icon(Icons.Rounded.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(addLabel)
        }
    }
}

@Composable
private fun PlanRowCard(row: PlanRow, currency: Currency, onClick: () -> Unit) {
    val item = row.item
    val isIncome = item.type == EntryType.INCOME
    val subtitle = buildString {
        if (!item.isActive) append("Отключено · ")
        append(scheduleText(item))
        if (!isIncome) append(if (item.isEssential) " · обязательный" else " · необязательный")
    }
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().alpha(if (item.isActive) 1f else 0.55f),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            CategoryAvatar(row.category?.icon ?: "other")
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                SecondaryText(subtitle)
                if (item.frequency == Frequency.WEEKLY || item.frequency == Frequency.DAILY) {
                    SecondaryText("${row.occurrences}× в этом месяце = ${MoneyFormat.compact(row.monthTotal)}")
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatMoney(item.amount, currency, showPlus = isIncome, withCurrency = false),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isIncome) BudgetTheme.extra.income else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                val unit = frequencyUnit(item.frequency)
                SecondaryText(if (unit.isEmpty()) currency.symbol else "${currency.symbol} $unit")
            }
        }
    }
}
