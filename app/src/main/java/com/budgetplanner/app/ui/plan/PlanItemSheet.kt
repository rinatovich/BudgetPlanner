package com.budgetplanner.app.ui.plan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.budgetplanner.app.ui.components.AmountField
import com.budgetplanner.app.ui.components.AppBottomSheet
import com.budgetplanner.app.ui.components.AppDatePickerDialog
import com.budgetplanner.app.ui.components.CategoryStrip
import com.budgetplanner.app.ui.components.ConfirmDialog
import com.budgetplanner.app.ui.components.DayOfMonthPicker
import com.budgetplanner.app.ui.components.FormRow
import com.budgetplanner.app.ui.components.GroupCard
import com.budgetplanner.app.ui.components.InlineTextField
import com.budgetplanner.app.ui.components.NewCategoryDialog
import com.budgetplanner.app.ui.components.PickerPill
import com.budgetplanner.app.ui.components.PrimaryButton
import com.budgetplanner.app.ui.components.RowDivider
import com.budgetplanner.app.ui.components.SectionFooter
import com.budgetplanner.app.ui.components.SheetHorizontal
import com.budgetplanner.app.ui.components.SwitchRow
import com.budgetplanner.app.ui.components.TextAction
import com.budgetplanner.app.ui.components.WeekdayPicker
import com.budgetplanner.app.ui.components.animatedLong
import com.budgetplanner.app.ui.components.bouncyClickable
import com.budgetplanner.app.ui.components.formatMoney
import com.budgetplanner.app.ui.components.frequencyLabel
import com.budgetplanner.app.ui.components.fullDate
import com.budgetplanner.app.ui.components.parseAmount
import com.budgetplanner.app.ui.theme.AppText
import com.budgetplanner.app.ui.theme.BudgetTheme
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
    val c = BudgetTheme.colors
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
    var detailsExpanded by remember { mutableStateOf(!initial?.note.isNullOrEmpty()) }
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
        Column(Modifier.padding(horizontal = SheetHorizontal)) {
            Text(
                text = when {
                    initial != null -> "Изменить"
                    isExpense -> "Новый плановый расход"
                    else -> "Новый плановый доход"
                },
                style = AppText.title3,
                color = c.label,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            )
            Spacer(Modifier.height(20.dp))
            AmountField(
                digits = amountDigits,
                onDigitsChange = {
                    amountDigits = it
                    amountError = false
                },
                currencySymbol = currency.symbol,
                autoFocus = initial == null,
                isError = amountError,
                errorText = if (amountError) "Сумма должна быть больше 0" else null,
            )
            Spacer(Modifier.height(20.dp))
            Text(
                "Категория",
                style = AppText.footnote,
                color = c.secondaryLabel,
                modifier = Modifier.padding(start = 4.dp, bottom = 10.dp),
            )
        }

        CategoryStrip(
            categories = categories,
            selectedId = categoryId,
            onSelect = { categoryId = it.id },
            onNew = { showNewCategory = true },
        )

        Column(Modifier.padding(horizontal = SheetHorizontal)) {
            Spacer(Modifier.height(16.dp))

            GroupCard(Modifier.animateContentSize()) {
                Text(
                    "Как часто",
                    style = AppText.footnote,
                    color = c.secondaryLabel,
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 8.dp),
                )
                LazyRow(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(frequencies.size) { i ->
                        val f = frequencies[i]
                        PickerPill(frequencyLabel(f), selected = frequency == f, onClick = { frequency = f })
                    }
                }
                Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    when (frequency) {
                        Frequency.WEEKLY -> WeekdayPicker(dayOfWeek, firstDayOfWeek, onSelect = { dayOfWeek = it })
                        Frequency.MONTHLY -> DayOfMonthPicker(dayOfMonth, onSelect = { dayOfMonth = it })
                        Frequency.ONCE, Frequency.YEARLY -> Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (frequency == Frequency.ONCE) "Дата" else "Повторяется каждый год",
                                style = AppText.body,
                                color = c.label,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                fullDate(date),
                                style = AppText.body.copy(fontWeight = FontWeight.Medium),
                                color = c.blue,
                                modifier = Modifier
                                    .bouncyClickable(scale = 0.94f) { showDatePicker = true }
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(c.blue.copy(alpha = 0.12f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                            )
                        }
                        Frequency.DAILY -> Text("Операция повторяется каждый день месяца.", style = AppText.subhead, color = c.secondaryLabel)
                    }
                }
            }

            if (isExpense || initial != null) {
                Spacer(Modifier.height(16.dp))
                GroupCard {
                    if (isExpense) {
                        SwitchRow("Обязательный расход", essential, onCheckedChange = {
                            essential = it
                            essentialTouched = true
                        })
                    }
                    if (isExpense && initial != null) RowDivider()
                    if (initial != null) SwitchRow("Учитывать в плане", active, onCheckedChange = { active = it })
                }
            }

            Column(Modifier.animateContentSize()) {
                Spacer(Modifier.height(8.dp))
                if (detailsExpanded) {
                    GroupCard {
                        FormRow("Название") {
                            InlineTextField(title, { title = it.take(40) }, placeholder = "Необязательно", modifier = Modifier.weight(1f))
                        }
                        RowDivider()
                        FormRow("Заметка") {
                            InlineTextField(note, { note = it.take(200) }, placeholder = "Необязательно", modifier = Modifier.weight(1f))
                        }
                    }
                } else {
                    TextAction("Название и заметка", onClick = { detailsExpanded = true }, modifier = Modifier.fillMaxWidth())
                }
            }

            AnimatedVisibility(
                visible = preview != null && preview.delta != 0L,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                if (preview != null) PreviewCard(preview, currency)
            }

            Spacer(Modifier.height(16.dp))
            PrimaryButton("Сохранить", onClick = {
                val item = buildItem()
                if (item == null || item.amount <= 0L) {
                    amountError = true
                } else {
                    onSave(item)
                    dismiss()
                }
            })
            if (initial != null) {
                TextAction("Удалить", onClick = { showDelete = true }, color = c.red, modifier = Modifier.fillMaxWidth())
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
            text = "Операция исчезнет из плана во всех месяцах. Удаление можно отменить.",
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

/** «Было → станет»: влияние на свободный остаток месяца, цифры плавно докручиваются. */
@Composable
private fun PreviewCard(preview: ScenarioResult, currency: Currency) {
    val c = BudgetTheme.colors
    val after = animatedLong(preview.freeAfter)
    val positive = preview.delta > 0
    Column(Modifier.padding(top = 16.dp)) {
        GroupCard {
            Column(Modifier.padding(16.dp)) {
                Text("Свободно в месяце", style = AppText.footnote, color = c.secondaryLabel)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        formatMoney(preview.freeBefore, currency, withCurrency = false),
                        style = AppText.body.copy(fontFeatureSettings = "tnum"),
                        color = c.secondaryLabel,
                    )
                    Text("  →  ", style = AppText.body, color = c.tertiaryLabel)
                    Text(
                        formatMoney(after, currency),
                        style = AppText.headline.copy(fontFeatureSettings = "tnum"),
                        color = if (after < 0) c.red else c.label,
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    formatMoney(preview.delta, currency, showPlus = true),
                    style = AppText.footnote.copy(fontFeatureSettings = "tnum"),
                    color = if (positive) c.green else c.red,
                )
            }
        }
        SectionFooter("Так изменится остаток после сохранения.")
    }
}
