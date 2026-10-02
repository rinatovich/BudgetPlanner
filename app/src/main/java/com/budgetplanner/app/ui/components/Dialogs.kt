@file:OptIn(ExperimentalMaterial3Api::class)

package com.budgetplanner.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.budgetplanner.app.ui.theme.AppText
import com.budgetplanner.app.ui.theme.BudgetTheme
import kotlinx.coroutines.delay

/** Алерт в стиле iOS: компактная карточка по центру, кнопки разделены тонкими линиями. */
@Composable
fun IosAlert(
    title: String,
    onDismiss: () -> Unit,
    confirmLabel: String,
    onConfirm: () -> Unit,
    message: String? = null,
    confirmEnabled: Boolean = true,
    destructive: Boolean = false,
    content: (@Composable () -> Unit)? = null,
) {
    val c = BudgetTheme.colors
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) { appear.animateTo(1f, spring(dampingRatio = 0.8f, stiffness = 500f)) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier
                .width(280.dp)
                .graphicsLayer {
                    val s = 0.88f + 0.12f * appear.value
                    scaleX = s
                    scaleY = s
                    alpha = appear.value.coerceIn(0f, 1f)
                }
                .clip(RoundedCornerShape(20.dp))
                .background(c.elevated),
        ) {
            Column(
                Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(title, style = AppText.headline, color = c.label, textAlign = TextAlign.Center)
                if (message != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(message, style = AppText.footnote, color = c.secondaryLabel, textAlign = TextAlign.Center)
                }
                if (content != null) {
                    Spacer(Modifier.height(14.dp))
                    content()
                }
            }
            HorizontalDivider(thickness = 0.5.dp, color = c.separator)
            Row(Modifier.fillMaxWidth().height(48.dp)) {
                AlertButton("Отмена", c.blue, bold = false, enabled = true, onClick = onDismiss, modifier = Modifier.weight(1f))
                Box(Modifier.width(0.5.dp).fillMaxHeight().background(c.separator))
                AlertButton(
                    confirmLabel,
                    if (destructive) c.red else c.blue,
                    bold = true,
                    enabled = confirmEnabled,
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun AlertButton(
    label: String,
    color: Color,
    bold: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = BudgetTheme.colors
    Box(
        modifier = if (enabled) modifier.fillMaxHeight().rowClickable(base = c.elevated, onClick = onClick) else modifier.fillMaxHeight(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = AppText.body.copy(fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal),
            color = if (enabled) color else color.copy(alpha = 0.4f),
        )
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    destructive: Boolean = true,
) {
    IosAlert(
        title = title,
        message = text,
        confirmLabel = confirmLabel,
        destructive = destructive,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

@Composable
fun NewCategoryDialog(onCreate: (String) -> Unit, onDismiss: () -> Unit) {
    val c = BudgetTheme.colors
    var name by remember { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        delay(200)
        runCatching { focus.requestFocus() }
    }
    IosAlert(
        title = "Новая категория",
        confirmLabel = "Создать",
        confirmEnabled = name.isNotBlank(),
        onConfirm = { if (name.isNotBlank()) onCreate(name.trim()) },
        onDismiss = onDismiss,
    ) {
        BasicTextField(
            value = name,
            onValueChange = { name = it.take(30) },
            singleLine = true,
            textStyle = AppText.body.copy(color = c.label),
            cursorBrush = SolidColor(c.blue),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focus)
                .clip(RoundedCornerShape(10.dp))
                .background(c.fill)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            decorationBox = { inner ->
                Box {
                    if (name.isEmpty()) Text("Название", style = AppText.body, color = c.tertiaryLabel)
                    inner()
                }
            },
        )
    }
}

/**
 * Смахивание влево, как в iOS. Красная подложка открывается под строкой.
 * [removeImmediately] = true: строка уходит сразу, а удаление можно отменить в тосте.
 * false: смахивание лишь запрашивает удаление (строка возвращается — решает диалог).
 */
@Composable
fun SwipeToDeleteBox(
    shape: Shape,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    removeImmediately: Boolean = true,
    content: @Composable () -> Unit,
) {
    val c = BudgetTheme.colors
    val thud = rememberThud()
    val currentOnDelete by rememberUpdatedState(onDelete)
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                thud()
                currentOnDelete()
                removeImmediately
            } else {
                false
            }
        },
    )
    SwipeToDismissBox(
        state = state,
        modifier = modifier.clip(shape),
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier.fillMaxSize().background(c.red).padding(end = 24.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(Icons.Rounded.Delete, contentDescription = "Удалить", tint = Color.White)
            }
        },
        content = { content() },
    )
}
