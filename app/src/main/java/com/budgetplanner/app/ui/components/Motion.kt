package com.budgetplanner.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.budgetplanner.app.ui.theme.BudgetTheme

/** Кривая «как в iOS»: быстрый старт и мягкое торможение. */
val IosEasing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

/** Лёгкий «тик» — выбор сегмента, переключение, движение по графику. */
@Composable
fun rememberTick(): () -> Unit {
    val haptics = LocalHapticFeedback.current
    return remember(haptics) { { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove) } }
}

/** Более плотный отклик — сохранение, удаление, ошибка. */
@Composable
fun rememberThud(): () -> Unit {
    val haptics = LocalHapticFeedback.current
    return remember(haptics) { { haptics.performHapticFeedback(HapticFeedbackType.LongPress) } }
}

/**
 * Нажимаемый элемент с пружинным «проседанием», как у кнопок в iOS.
 * Без ряби (ripple) — в iOS её нет.
 */
@Composable
fun Modifier.bouncyClickable(
    scale: Float = 0.96f,
    tick: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val s by animateFloatAsState(
        targetValue = if (pressed && enabled) scale else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 700f),
        label = "press-scale",
    )
    val haptics = LocalHapticFeedback.current
    return this
        .graphicsLayer {
            scaleX = s
            scaleY = s
        }
        .clickable(interactionSource = source, indication = null, enabled = enabled) {
            if (tick) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        }
}

/**
 * Строка списка: при нажатии фон мгновенно темнеет и плавно возвращается — как в Настройках iOS.
 */
@Composable
fun Modifier.rowClickable(
    base: Color = BudgetTheme.colors.cell,
    onClick: () -> Unit,
): Modifier {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val bg by animateColorAsState(
        targetValue = if (pressed) BudgetTheme.colors.cellPressed else base,
        animationSpec = if (pressed) snap() else tween(280),
        label = "row-press",
    )
    return this
        .background(bg)
        .clickable(interactionSource = source, indication = null, onClick = onClick)
}

/**
 * Каскадное появление блока: снизу вверх с задержкой по [index].
 * Пока [revealed] = false, блок скрыт; при его смене на true — плавно выезжает.
 */
@Composable
fun Modifier.staggerIn(revealed: Boolean, index: Int): Modifier {
    val p by animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = if (revealed) tween(460, delayMillis = index * 50, easing = IosEasing) else snap(),
        label = "stagger",
    )
    return this.graphicsLayer {
        alpha = p
        translationY = (1f - p) * 26.dp.toPx()
    }
}
