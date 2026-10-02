package com.budgetplanner.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.budgetplanner.app.ui.theme.AppText
import com.budgetplanner.app.ui.theme.BudgetTheme
import com.budgetplanner.domain.model.LAST_DAY_OF_MONTH
import java.time.DayOfWeek

@Composable
private fun pickerBackground(selected: Boolean): Color {
    val c = BudgetTheme.colors
    val color by animateColorAsState(if (selected) c.blue else c.fill, tween(200), label = "picker-bg")
    return color
}

@Composable
private fun pickerForeground(selected: Boolean): Color {
    val c = BudgetTheme.colors
    val color by animateColorAsState(if (selected) Color.White else c.label, tween(200), label = "picker-fg")
    return color
}

@Composable
private fun Modifier.pickable(selected: Boolean, onClick: () -> Unit): Modifier {
    val tick = rememberTick()
    return this.selectable(
        selected = selected,
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        role = Role.RadioButton,
        onClick = {
            if (!selected) tick()
            onClick()
        },
    )
}

@Composable
fun PickerCircle(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(pickerBackground(selected))
            .pickable(selected, onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = AppText.subhead.copy(fontWeight = FontWeight.Medium),
            color = pickerForeground(selected),
        )
    }
}

@Composable
fun PickerPill(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(CircleShape)
            .background(pickerBackground(selected))
            .pickable(selected, onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = AppText.subhead.copy(fontWeight = FontWeight.Medium),
            color = pickerForeground(selected),
            maxLines = 1,
        )
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
    LazyRow(state = state, modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(DAYS_1_TO_31.size) { index ->
            val day = DAYS_1_TO_31[index]
            PickerCircle(label = day.toString(), selected = selected == day, onClick = { onSelect(day) })
        }
        item {
            PickerPill(
                label = "Последний день",
                selected = selected == LAST_DAY_OF_MONTH,
                onClick = { onSelect(LAST_DAY_OF_MONTH) },
                modifier = Modifier.padding(vertical = 3.dp),
            )
        }
    }
}

private val DAYS_1_TO_31: List<Int> = (1..31).toList()
