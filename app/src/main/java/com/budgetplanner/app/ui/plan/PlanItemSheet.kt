@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.budgetplanner.app.ui.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.budgetplanner.app.ui.components.AmountField
import com.budgetplanner.app.ui.components.AppBottomSheet
import com.budgetplanner.app.ui.components.AppDatePickerDialog
import com.budgetplanner.app.ui.components.CategoryChip
import com.budgetplanner.app.ui.components.ConfirmDialog
import com.budgetplanner.app.ui.components.DayOfMonthPicker
import com.budgetplanner.app.ui.components.NewCategoryDialog
import com.budgetplanner.app.ui.components.SecondaryText
import com.budgetplanner.app.ui.components.SegmentedChoice
import com.budgetplanner.app.ui.components.WeekdayPicker
import com.budgetplanner.app.ui.components.formatMoney
import com.budgetplanner.app.ui.components.frequencyLabel
import com.budgetplanner.app.ui.components.fullDate
import com.budgetplanner.app.ui.components.parseAmount
import com.budgetplanner.domain.calc.ScenarioResult
import com.budgetplanner.domain.model.Category
import com.budgetplanner.domain.model.Currency
import com.budgetplanner.domain.model.EntryType
import com.budgetplanner.domain.model.Frequency
import com.budgetplanner.domain.model.PlanItem
import java.time.DayOfWeek
import java.time.LocalDate

/** Форма добавления/редактирования плановой операции (регулярной или единоразовой). */
@Composable
fun PlanItemSheet(
    type: EntryType,
    initial: PlanItem?,
    categories: List<Category>,
    currency: Currency,
    firstDayOfWeek: DayOfWeek,
    defaultStartDate: LocalDate,
    simulate: (PlanItem) -> ScenarioResult?,
    onSave: (PlanItem) -> Unit,
    onDelete: (PlanItem) -> Unit,
    onCreateCategory: (String, (Long) -> Unit) -> Unit,
    onDismiss: () -> Unit,
) {
    val today = remember { LocalDate.now() }
    val isExpense = type == EntryType.EXPENSE

    var categoryId by remember { mutableStateOf(initial?.categoryId) }
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var amountDigits by remember { mutableStateOf(initial?.amount?.toString() ?: "") }
    var amountError by remember { mutableStateOf(false) }
    var frequency by remember { mutableStateOf(initial?.frequency ?: Frequency.MONTHLY) }
    var dayOfWeek by remember { mutableStateOf(initial?.dayOfWeek ?: initial?.startDate?.dayOfWeek ?: today.dayOfWeek) }
    var dayOfMonth by remember { mutableIntStateOf(initial?.dayOfMonth ?: initial?.startDate?.dayOfMonth ?: today.dayOfMonth) }
    var date by remember { mutableStateOf(initial?.startDate ?: today) }
    var essential by remember { mutableStateOf(initial?.isEssential ?: false) }
    var essentialTouched by remember { mutableStateOf(initial != null) }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var noteExpanded by remember { mutableStateOf(!initial?.note.isNullOrEmpty()) }
    var active by remember { mutableStateOf(initial?.isActive ?: true) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var showNewCategory by remember { mutableStateOf(false) }

    LaunchedEffect(categories) {
        if (categoryId == null && categories.isNotEmpty()) categoryId = categories.first().id
    }
    LaunchedEffect(categoryId) {
        if (isExpense && !essentialTouched) {
            essential = categories.firstOrNull { it.id == categoryId }?.isEssentialDefault ?: false
        }
    }

    fun buildItem(): PlanItem? {
        val cat = categoryId ?: return null
        val categoryName = categories.firstOrNull { it.id == cat }?.name ?: ""
        val keepsOriginalStart = initial != null &&
            (initial.frequency == Frequency.MONTHLY || initial.frequency == Frequency.WEEKLY || initial.frequency == Frequency.DAILY)
        val start = when (frequency) {
            Frequency.ONCE, Frequency.YEARLY -> date
            else -> if (keepsOriginalStart && initial != null) initial.startDate else defaultStartDate
        }
        return PlanItem(
            id = initial?.id ?: 0L,
            title = title.trim().ifEmpty { categoryName },
            amount = parseAmount(amountDigits),
            type = type,
            frequency = frequency,
            dayOfWeek = if (frequency == Frequency.WEEKLY) dayOfWeek else null,
            dayOfMonth = if (frequency == Frequency.MONTHLY) dayOfMonth else null,
            startDate = start,
            endDate = initial?.endDate,
            isActive = active,
            categoryId = cat,
            isEssential = isExpense && essential,
            note = note.trim(),
        )
    }

    val preview: ScenarioResult? = remember(amountDigits, categoryId, frequency, dayOfWeek, dayOfMonth, date, essential, active) {
        buildItem()?.takeIf { it.amount > 0 }?.let(simulate)
    }

    val frequencies = remember(type) {
        listOf(Frequency.MONTHLY, Frequency.WEEKLY, Frequency.DAILY, Frequency.ONCE, Frequency.YEARLY)
            .filter { isExpense || it != Frequency.DAILY }
    }

    AppBottomSheet(onDismiss = onDismiss) { dismiss ->
        Text(
            text = when {
                initial != null -> "Изменить"
                isExpense -> "Новый плановый расход"
                else -> "Новый плановый доход"
            },
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(16.dp))

        SecondaryText("Категория")
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            categories.forEach { category ->
                CategoryChip(category, selected = category.id == categoryId, onClick = { categoryId = category.id })
            }
            AssistChip(
                onClick = { showNewCategory = true },
                label = { Text("Новая") },
                leadingIcon = { Icon(Icons.Rounded.Add, contentDescription = null) },
            )
        }
        Spacer(Modifier.height(16.dp))

        Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant) {
            AmountField(
                digits = amountDigits,
                onDigitsChange = {
                    amountDigits = it
                    amountError = false
                },
                currencySymbol = currency.symbol,
                isError = amountError,
                errorText = if (amountError) "Сумма должна быть больше 0" else null,
            )
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = title,
            onValueChange = { title = it.take(40) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Название (необязательно)") },
        )
        Spacer(Modifier.height(16.dp))

        SecondaryText("Как часто")
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            frequencies.forEach { f ->
                FilterChip(
                    selected = frequency == f,
                    onClick = { frequency = f },
                    label = { Text(frequencyLabel(f)) },
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        when (frequency) {
            Frequency.WEEKLY -> {
                SecondaryText("День недели")
                Spacer(Modifier.height(8.dp))
                WeekdayPicker(dayOfWeek, firstDayOfWeek, onSelect = { dayOfWeek = it })
            }
            Frequency.MONTHLY -> {
                SecondaryText("День месяца")
                Spacer(Modifier.height(8.dp))
                DayOfMonthPicker(dayOfMonth, onSelect = { dayOfMonth = it })
            }
            Frequency.ONCE, Frequency.YEARLY -> {
                SecondaryText(if (frequency == Frequency.ONCE) "Дата" else "Дата (повторяется каждый год)")
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Rounded.CalendarMonth, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(fullDate(date))
                }
            }
            Frequency.DAILY -> SecondaryText("Операция повторяется каждый день месяца.")
        }

        if (isExpense) {
            Spacer(Modifier.height(16.dp))
            SegmentedChoice(
                options = listOf("Обязательный", "Необязательный"),
                selectedIndex = if (essential) 0 else 1,
                onSelect = {
                    essential = it == 0
                    essentialTouched = true
                },
            )
        }

        Spacer(Modifier.height(8.dp))
        if (noteExpanded) {
            OutlinedTextField(
                value = note,
                onValueChange = { note = it.take(200) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Комментарий") },
                minLines = 2,
            )
        } else {
            TextButton(onClick = { noteExpanded = true }) { Text("Добавить комментарий") }
        }

        if (initial != null) {
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Учитывать в плане", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = active, onCheckedChange = { active = it })
            }
        }

        if (preview != null && preview.delta != 0L) {
            Spacer(Modifier.height(8.dp))
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                androidx.compose.foundation.layout.Column(Modifier.padding(16.dp)) {
                    Text(
                        "Свободно в месяце: было → станет",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${formatMoney(preview.freeBefore, currency)} → ${formatMoney(preview.freeAfter, currency)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                val item = buildItem()
                if (item == null || item.amount <= 0L) {
                    amountError = true
                } else {
                    onSave(item)
                    dismiss()
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) { Text("Сохранить") }

        if (initial != null) {
            TextButton(onClick = { showDelete = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Удалить", color = MaterialTheme.colorScheme.error)
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
                onCreateCategory(name) { id -> categoryId = id }
            },
            onDismiss = { showNewCategory = false },
        )
    }
    if (showDelete && initial != null) {
        ConfirmDialog(
            title = "Удалить «${initial.title}»?",
            text = "Операция исчезнет из плана во всех месяцах.",
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
