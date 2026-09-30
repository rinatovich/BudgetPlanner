package com.budgetplanner.app.ui.operations

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.budgetplanner.app.presentation.OperationsUiState
import com.budgetplanner.app.ui.components.CategoryAvatar
import com.budgetplanner.app.ui.components.ConfirmDialog
import com.budgetplanner.app.ui.components.EmptyState
import com.budgetplanner.app.ui.components.MonthSwitcher
import com.budgetplanner.app.ui.components.SecondaryText
import com.budgetplanner.app.ui.components.SwipeToDeleteBox
import com.budgetplanner.app.ui.components.dayLabel
import com.budgetplanner.app.ui.components.formatMoney
import com.budgetplanner.app.ui.theme.BudgetTheme
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
    padding: PaddingValues,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onReset: () -> Unit,
    onEdit: (Transaction) -> Unit,
    onDelete: (Long) -> Unit,
) {
    var pendingDelete by remember { mutableStateOf<Transaction?>(null) }
    val today = remember { LocalDate.now() }

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = padding.calculateTopPadding() + 4.dp,
            bottom = padding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { MonthSwitcher(month, onPrevious, onNext, onReset) }

        if (state == null) {
            item {
                Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else if (state.groups.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Rounded.Receipt,
                    title = "Операций пока нет",
                    subtitle = "Нажмите «+», чтобы записать расход или доход. Это занимает пару секунд.",
                )
            }
        } else {
            state.groups.forEach { group ->
                item(key = "header-${group.date}") {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 12.dp, start = 4.dp, end = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            dayLabel(group.date, today),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        SecondaryText(formatMoney(group.total, state.currency, showPlus = true))
                    }
                }
                items(group.transactions.size, key = { "tx-${group.transactions[it].id}" }) { index ->
                    val tx = group.transactions[index]
                    SwipeToDeleteBox(onRequestDelete = { pendingDelete = tx }) {
                        TransactionRow(tx, state.categories[tx.categoryId], state.currency, onClick = { onEdit(tx) })
                    }
                }
            }
        }
    }

    val target = pendingDelete
    if (target != null) {
        ConfirmDialog(
            title = "Удалить операцию?",
            text = "«${target.title}» на сумму ${formatMoney(target.amount, state?.currency ?: Currency.UZS)} будет удалена.",
            confirmLabel = "Удалить",
            onConfirm = {
                onDelete(target.id)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun TransactionRow(tx: Transaction, category: Category?, currency: Currency, onClick: () -> Unit) {
    val isIncome = tx.type == EntryType.INCOME
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            CategoryAvatar(category?.icon ?: "other", size = 40.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(tx.title, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                val sub = listOfNotNull(category?.name?.takeIf { it != tx.title }, tx.note.takeIf { it.isNotBlank() })
                    .joinToString(" · ")
                if (sub.isNotEmpty()) SecondaryText(sub)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = formatMoney(if (isIncome) tx.amount else -tx.amount, currency, showPlus = isIncome, withCurrency = false),
                style = MaterialTheme.typography.titleMedium,
                color = if (isIncome) BudgetTheme.extra.income else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
        }
    }
}
