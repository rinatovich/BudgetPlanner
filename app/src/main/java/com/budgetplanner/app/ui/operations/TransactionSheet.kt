@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.budgetplanner.app.ui.operations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.budgetplanner.app.ui.components.AmountField
import com.budgetplanner.app.ui.components.AppBottomSheet
import com.budgetplanner.app.ui.components.AppDatePickerDialog
import com.budgetplanner.app.ui.components.CategoryChip
import com.budgetplanner.app.ui.components.ConfirmDialog
import com.budgetplanner.app.ui.components.NewCategoryDialog
import com.budgetplanner.app.ui.components.SecondaryText
import com.budgetplanner.app.ui.components.SegmentedChoice
import com.budgetplanner.app.ui.components.dayLabel
import com.budgetplanner.app.ui.components.formatMoney
import com.budgetplanner.app.ui.components.parseAmount
import com.budgetplanner.domain.model.Category
import com.budgetplanner.domain.model.Currency
import com.budgetplanner.domain.model.EntryType
import com.budgetplanner.domain.model.Transaction
import java.time.LocalDate

/**
 * Быстрое добавление операции (initial == null) или редактирование существующей.
 *
 * Быстрый режим: ввести сумму → нажать категорию → готово.
 */
@Composable
fun TransactionSheet(
    initial: Transaction?,
    initialType: EntryType,
    expenseCategories: List<Category>,
    incomeCategories: List<Category>,
    lastExpense: Transaction?,
    lastExpenseCategoryName: String?,
    currency: Currency,
    onSave: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit,
    onCreateCategory: (String, EntryType, (Long) -> Unit) -> Unit,
    onPlanInstead: (EntryType) -> Unit,
    onDismiss: () -> Unit,
) {
    val isEdit = initial != null
    val today = remember { LocalDate.now() }

    var type by remember { mutableStateOf(initial?.type ?: initialType) }
    var digits by remember { mutableStateOf(initial?.amount?.toString() ?: "") }
    var amountError by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(initial?.categoryId) }
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var date by remember { mutableStateOf(initial?.date ?: today) }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var extrasExpanded by remember { mutableStateOf(isEdit) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showNewCategory by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    val categories = if (type == EntryType.EXPENSE) expenseCategories else incomeCategories

    AppBottomSheet(onDismiss = onDismiss) { dismiss ->

        fun save(categoryId: Long) {
            val amount = parseAmount(digits)
            if (amount <= 0L) {
                amountError = true
                return
            }
            val name = categories.firstOrNull { it.id == categoryId }?.name ?: ""
            onSave(
                Transaction(
                    id = initial?.id ?: 0L,
                    title = title.trim().ifEmpty { name },
                    amount = amount,
                    type = type,
                    categoryId = categoryId,
                    date = date,
                    note = note.trim(),
                ),
            )
            dismiss()
        }

        if (isEdit) {
            Text("Изменить операцию", style = MaterialTheme.typography.titleLarge)
        } else {
            SegmentedChoice(
                options = listOf("Расход", "Доход"),
                selectedIndex = if (type == EntryType.EXPENSE) 0 else 1,
                onSelect = {
                    type = if (it == 0) EntryType.EXPENSE else EntryType.INCOME
                    selectedCategory = null
                },
            )
        }
        Spacer(Modifier.height(12.dp))

        Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant) {
            AmountField(
                digits = digits,
                onDigitsChange = {
                    digits = it
                    amountError = false
                },
                currencySymbol = currency.symbol,
                autoFocus = !isEdit,
                isError = amountError,
                errorText = if (amountError) "Сумма должна быть больше 0" else null,
                textStyle = MaterialTheme.typography.headlineLarge,
            )
        }

        if (!isEdit && type == EntryType.EXPENSE && lastExpense != null) {
            Spacer(Modifier.height(12.dp))
            AssistChip(
                onClick = {
                    onSave(lastExpense.copy(id = 0L, date = today))
                    dismiss()
                },
                label = {
                    Text(
                        "Повторить: ${lastExpenseCategoryName ?: lastExpense.title} · " +
                            formatMoney(lastExpense.amount, currency, withCurrency = false),
                        maxLines = 1,
                    )
                },
                leadingIcon = { Icon(Icons.Rounded.Repeat, contentDescription = null) },
            )
        }

        Spacer(Modifier.height(16.dp))
        SecondaryText(if (isEdit) "Категория" else "Выберите категорию — операция сохранится сразу")
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            categories.forEach { category ->
                CategoryChip(
                    category = category,
                    selected = category.id == selectedCategory,
                    onClick = {
                        selectedCategory = category.id
                        if (!isEdit) save(category.id)
                    },
                )
            }
            AssistChip(
                onClick = { showNewCategory = true },
                label = { Text("Новая категория") },
                leadingIcon = { Icon(Icons.Rounded.Add, contentDescription = null) },
            )
        }

        Spacer(Modifier.height(8.dp))
        if (extrasExpanded) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it.take(40) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Название (необязательно)") },
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Rounded.CalendarMonth, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(dayLabel(date, today))
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it.take(200) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Комментарий") },
                minLines = 2,
            )
        } else {
            TextButton(onClick = { extrasExpanded = true }) { Text("Дата, название, комментарий") }
        }

        if (isEdit) {
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    val id = selectedCategory
                    if (id == null) amountError = parseAmount(digits) <= 0L else save(id)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text("Сохранить") }
            TextButton(onClick = { showDelete = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Удалить", color = MaterialTheme.colorScheme.error)
            }
        } else {
            TextButton(onClick = { onPlanInstead(type) }) {
                Text(
                    if (type == EntryType.EXPENSE) "Это регулярный расход? Добавить в план"
                    else "Это регулярный доход? Добавить в план",
                )
            }
        }
    }

    if (showDatePicker) {
        AppDatePickerDialog(
            initial = date,
            onPick = {
                date = it
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }
    if (showNewCategory) {
        NewCategoryDialog(
            onCreate = { name ->
                showNewCategory = false
                onCreateCategory(name, type) { id -> selectedCategory = id }
            },
            onDismiss = { showNewCategory = false },
        )
    }
    if (showDelete && initial != null) {
        ConfirmDialog(
            title = "Удалить операцию?",
            text = "Эту операцию нельзя будет вернуть.",
            confirmLabel = "Удалить",
            onConfirm = {
                showDelete = false
                onDelete(initial)
                onDismiss()
            },
            onDismiss = { showDelete = false },
        )
    }
}
