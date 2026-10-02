@file:OptIn(ExperimentalFoundationApi::class)

package com.budgetplanner.app.ui.operations

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.budgetplanner.app.presentation.OperationsUiState
import com.budgetplanner.app.ui.components.CategoryAvatar
import com.budgetplanner.app.ui.components.EmptyState
import com.budgetplanner.app.ui.components.LargeTitleScreen
import com.budgetplanner.app.ui.components.LoadingRow
import com.budgetplanner.app.ui.components.MonthSwitcher
import com.budgetplanner.app.ui.components.RowDivider
import com.budgetplanner.app.ui.components.SectionHeader
import com.budgetplanner.app.ui.components.SwipeToDeleteBox
import com.budgetplanner.app.ui.components.TodayChip
import com.budgetplanner.app.ui.components.dayLabel
import com.budgetplanner.app.ui.components.formatMoney
import com.budgetplanner.app.ui.components.groupShape
import com.budgetplanner.app.ui.components.rowClickable
import com.budgetplanner.app.ui.theme.AppText
import com.budgetplanner.app.ui.theme.BudgetTheme
import com.budgetplanner.app.ui.theme.MoneyStyles
import com.budgetplanner.domain.model.Category
import com.budgetplanner.domain.model.Currency
import com.budgetplanner.domain.model.EntryType
import com.budgetplanner.domain.model.Transaction
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun OperationsScreen(
    state: OperationsUiState?,
    month: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onReset: () -> Unit,
    onEdit: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit,
) {
    val today = remember { LocalDate.now() }

    LargeTitleScreen(
        title = "Операции",
        titleAccessory = { TodayChip(visible = month != YearMonth.now(), onClick = onReset) },
    ) {
        item(key = "month") { MonthSwitcher(month, onPrevious, onNext, onReset) }

        if (state == null) {
            item(key = "loading") { LoadingRow() }
        } else if (state.groups.isEmpty()) {
            item(key = "empty") {
                EmptyState(
                    icon = Icons.Rounded.Receipt,
                    title = "Операций пока нет",
                    subtitle = "Нажмите «+», чтобы записать расход или доход. Это занимает пару секунд.",
                )
            }
        } else {
            state.groups.forEach { group ->
                item(key = "header-${group.date}") {
                    SectionHeader(
                        title = dayLabel(group.date, today),
                        trailing = formatMoney(group.total, state.currency, showPlus = true),
                    )
                }
                itemsIndexed(group.transactions, key = { _, tx -> "tx-${tx.id}" }) { index, tx ->
                    SwipeToDeleteBox(
                        shape = groupShape(index, group.transactions.size),
                        onDelete = { onDelete(tx) },
                        modifier = Modifier.animateItemPlacement(),
                    ) {
                        TransactionRow(
                            tx = tx,
                            category = state.categories[tx.categoryId],
                            currency = state.currency,
                            showDivider = index < group.transactions.lastIndex,
                            onClick = { onEdit(tx) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(
    tx: Transaction,
    category: Category?,
    currency: Currency,
    showDivider: Boolean,
    onClick: () -> Unit,
) {
    val c = BudgetTheme.colors
    val isIncome = tx.type == EntryType.INCOME
    Column {
        Row(
            Modifier.fillMaxWidth().rowClickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryAvatar(category?.icon ?: "other")
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(tx.title, style = AppText.body, color = c.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val sub = listOfNotNull(category?.name?.takeIf { it != tx.title }, tx.note.takeIf { it.isNotBlank() })
                    .joinToString(" · ")
                if (sub.isNotEmpty()) {
                    Text(sub, style = AppText.footnote, color = c.secondaryLabel, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = formatMoney(if (isIncome) tx.amount else -tx.amount, currency, showPlus = isIncome, withCurrency = false),
                style = MoneyStyles.row,
                color = if (isIncome) c.green else c.label,
                maxLines = 1,
            )
        }
        if (showDivider) RowDivider(start = 62.dp)
    }
}
