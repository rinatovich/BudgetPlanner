package com.budgetplanner.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.budgetplanner.domain.model.LAST_DAY_OF_MONTH
import java.time.DayOfWeek

@Composable
private fun pickerBackground(selected: Boolean) =
    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant

@Composable
private fun pickerForeground(selected: Boolean) =
    if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

@Composable
fun PickerCircle(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(pickerBackground(selected))
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = pickerForeground(selected))
    }
}

@Composable
fun PickerPill(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(pickerBackground(selected))
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = pickerForeground(selected))
    }
}

/** Ряд из семи кружков «Пн … Вс». */
@Composable
fun WeekdayPicker(
    selected: DayOfWeek,
    firstDayOfWeek: DayOfWeek,
    onSelect: (DayOfWeek) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        orderedWeekdays(firstDayOfWeek).forEach { day ->
            PickerCircle(label = weekdayShort(day), selected = day == selected, onClick = { onSelect(day) })
        }
    }
}

/** Прокручиваемый ряд дней месяца 1…31 и «Последний день». */
@Composable
fun DayOfMonthPicker(selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val selectedIndex = if (selected == LAST_DAY_OF_MONTH) 31 else (selected - 1).coerceIn(0, 30)
    val state = rememberLazyListState(initialFirstVisibleItemIndex = (selectedIndex - 2).coerceAtLeast(0))
    val days = remember31()
    LazyRow(state = state, modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(days) { day ->
            PickerCircle(label = day.toString(), selected = selected == day, onClick = { onSelect(day) })
        }
        item {
            PickerPill(
                label = "Последний день",
                selected = selected == LAST_DAY_OF_MONTH,
                onClick = { onSelect(LAST_DAY_OF_MONTH) },
            )
        }
    }
}

private val DAYS_1_TO_31: List<Int> = (1..31).toList()

private fun remember31(): List<Int> = DAYS_1_TO_31
