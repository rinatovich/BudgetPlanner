package com.budgetplanner.app.ui.operations

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.budgetplanner.app.ui.components.AmountField
import com.budgetplanner.app.ui.components.AppBottomSheet
import com.budgetplanner.app.ui.components.AppDatePickerDialog
import com.budgetplanner.app.ui.components.CategoryStrip
import com.budgetplanner.app.ui.components.ConfirmDialog
import com.budgetplanner.app.ui.components.FormRow
import com.budgetplanner.app.ui.components.GroupCard
import com.budgetplanner.app.ui.components.InlineTextField
import com.budgetplanner.app.ui.components.NewCategoryDialog
import com.budgetplanner.app.ui.components.PrimaryButton
import com.budgetplanner.app.ui.components.RowDivider
import com.budgetplanner.app.ui.components.SegmentedChoice
import com.budgetplanner.app.ui.components.SheetHorizontal
import com.budgetplanner.app.ui.components.TextAction
import com.budgetplanner.app.ui.components.bouncyClickable
import com.budgetplanner.app.ui.components.dayLabel
import com.budgetplanner.app.ui.components.formatMoney
import com.budgetplanner.app.ui.components.parseAmount
import com.budgetplanner.app.ui.components.rememberThud
import com.budgetplanner.app.ui.theme.AppText
import com.budgetplanner.app.ui.theme.BudgetTheme
import com.budgetplanner.domain.model.Category
import com.budgetplanner.domain.model.Currency
import com.budgetplanner.domain.model.EntryType
import com.budgetplanner.domain.model.Transaction
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Быстрое добавление операции (initial == null) или редактирование существующей.
 *
 * Быстрый режим: ввести сумму → коснуться категории → готово.
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
    val c = BudgetTheme.colors
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
    var saving by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showNewCategory by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    val categories = if (type == EntryType.EXPENSE) expenseCategories else incomeCategories

    AppBottomSheet(onDismiss = onDismiss) { dismiss ->
        val scope = rememberCoroutineScope()
        val thud = rememberThud()

        fun save(categoryId: Long) {
            if (saving) return
            val amount = parseAmount(digits)
            if (amount <= 0L) {
                amountError = true
                return
            }
            saving = true
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
            thud()
            dismiss()
        }

        Column(Modifier.padding(horizontal = SheetHorizontal)) {
            if (isEdit) {
                Text(
                    "Изменить операцию",
                    style = AppText.title3,
                    color = c.label,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                )
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
            Spacer(Modifier.height(20.dp))

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
            )

            if (!isEdit && type == EntryType.EXPENSE && lastExpense != null) {
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Row(
                        Modifier
                            .bouncyClickable(scale = 0.95f, tick = true) {
                                onSave(lastExpense.copy(id = 0L, date = today))
                                thud()
                                dismiss()
                            }
                            .clip(CircleShape)
                            .background(c.fill)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.Repeat, contentDescription = null, tint = c.blue, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Повторить: ${lastExpenseCategoryName ?: lastExpense.title} · " +
                                formatMoney(lastExpense.amount, currency, withCurrency = false),
                            style = AppText.subhead,
                            color = c.label,
                            maxLines = 1,
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(
                text = if (isEdit) "Категория" else "Выберите категорию — операция сохранится сразу",
                style = AppText.footnote,
                color = c.secondaryLabel,
                modifier = Modifier.padding(start = 4.dp, bottom = 10.dp),
            )
        }

        CategoryStrip(
            categories = categories,
            selectedId = selectedCategory,
            onSelect = { category ->
                selectedCategory = category.id
                if (!isEdit) {
                    // Даём кольцу выбора проявиться и только потом закрываем шторку.
                    scope.launch {
                        delay(160)
                        save(category.id)
                    }
                }
            },
            onNew = { showNewCategory = true },
            newLabel = "Новая",
        )

        Column(Modifier.padding(horizontal = SheetHorizontal).animateContentSize()) {
            Spacer(Modifier.height(12.dp))
            if (extrasExpanded) {
                GroupCard {
                    FormRow("Название") {
                        InlineTextField(title, { title = it.take(40) }, placeholder = "Необязательно", modifier = Modifier.weight(1f))
                    }
                    RowDivider()
                    FormRow("Дата", onClick = { showDatePicker = true }) {
                        Text(dayLabel(date, today), style = AppText.body, color = c.blue, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    }
                    RowDivider()
                    FormRow("Заметка") {
                        InlineTextField(note, { note = it.take(200) }, placeholder = "Необязательно", modifier = Modifier.weight(1f))
                    }
                }
            } else {
                TextAction("Дата, название, заметка", onClick = { extrasExpanded = true }, modifier = Modifier.fillMaxWidth())
            }

            if (isEdit) {
                Spacer(Modifier.height(16.dp))
                PrimaryButton("Сохранить", onClick = {
                    val id = selectedCategory
                    if (id == null) amountError = parseAmount(digits) <= 0L else save(id)
                })
                TextAction("Удалить", onClick = { showDelete = true }, color = c.red, modifier = Modifier.fillMaxWidth())
            } else {
                TextAction(
                    text = if (type == EntryType.EXPENSE) "Это регулярный расход? Добавить в план" else "Это регулярный доход? Добавить в план",
                    onClick = { onPlanInstead(type) },
                    modifier = Modifier.fillMaxWidth(),
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
            text = "Её можно будет вернуть сразу после удаления.",
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
