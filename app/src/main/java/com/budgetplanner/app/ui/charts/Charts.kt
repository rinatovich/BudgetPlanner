package com.budgetplanner.app.ui.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.budgetplanner.app.ui.components.IosEasing
import com.budgetplanner.app.ui.components.rememberTick
import com.budgetplanner.app.ui.theme.AppText
import com.budgetplanner.app.ui.theme.BudgetTheme
import com.budgetplanner.domain.calc.DailyBalance
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Баланс по дням. Линия «рисуется» при появлении; если остаток уходит в минус — эта часть красная.
 * Проведите пальцем по графику: [onScrub] получает выбранный день (null — палец отпущен).
 */
@Composable
fun BalanceLineChart(
    points: List<DailyBalance>,
    lowest: DailyBalance?,
    todayDay: Int?,
    onScrub: (DailyBalance?) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (points.size < 2) return
    val c = BudgetTheme.colors
    val tick = rememberTick()
    val progress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(durationMillis = 1000, easing = IosEasing))
    }
    var scrubIndex by remember { mutableIntStateOf(-1) }

    val maxV = max(points.maxOf { it.balance }, 0L)
    val minV = minOf(points.minOf { it.balance }, 0L)
    val lineColor = c.blue
    val warnColor = c.red
    val gridColor = c.separator
    val textColor = c.secondaryLabel
    val ringColor = c.cell

    Column(modifier) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(150.dp)
                .pointerInput(points) {
                    fun select(x: Float) {
                        val i = ((x / size.width) * (points.size - 1)).roundToInt().coerceIn(0, points.size - 1)
                        if (i != scrubIndex) {
                            scrubIndex = i
                            tick()
                            onScrub(points[i])
                        }
                    }
                    fun release() {
                        scrubIndex = -1
                        onScrub(null)
                    }
                    detectHorizontalDragGestures(
                        onDragStart = { select(it.x) },
                        onDragEnd = { release() },
                        onDragCancel = { release() },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            select(change.position.x)
                        },
                    )
                }
                .pointerInput(points) {
                    detectTapGestures(
                        onPress = { offset ->
                            val i = ((offset.x / size.width) * (points.size - 1)).roundToInt().coerceIn(0, points.size - 1)
                            scrubIndex = i
                            tick()
                            onScrub(points[i])
                            tryAwaitRelease()
                            scrubIndex = -1
                            onScrub(null)
                        },
                    )
                },
        ) {
            val padV = 10.dp.toPx()
            val h = size.height - padV * 2
            val w = size.width
            val range = (maxV - minV).toFloat().coerceAtLeast(1f)
            fun x(i: Int) = w * i / (points.size - 1)
            fun y(v: Long) = padV + h * (1f - (v - minV).toFloat() / range)
            val zeroY = y(0)

            if (minV < 0) {
                drawLine(
                    color = gridColor, start = Offset(0f, zeroY), end = Offset(w, zeroY),
                    strokeWidth = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)),
                )
            }

            val line = Path()
            points.forEachIndexed { i, p ->
                if (i == 0) line.moveTo(x(i), y(p.balance)) else line.lineTo(x(i), y(p.balance))
            }
            val area = Path().apply {
                addPath(line)
                lineTo(w, zeroY)
                lineTo(0f, zeroY)
                close()
            }
            val stroke = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

            clipRect(left = 0f, top = 0f, right = w * progress.value, bottom = size.height) {
                clipRect(left = 0f, top = 0f, right = w, bottom = zeroY) {
                    drawPath(area, Brush.verticalGradient(listOf(lineColor.copy(alpha = 0.22f), Color.Transparent)))
                    drawPath(line, color = lineColor, style = stroke)
                }
                if (minV < 0) {
                    clipRect(left = 0f, top = zeroY, right = w, bottom = size.height) {
                        drawPath(line, color = warnColor, style = stroke)
                    }
                }
            }

            fun marker(i: Int, color: Color, radius: Dp) {
                val center = Offset(x(i), y(points[i].balance))
                drawCircle(color = ringColor, radius = radius.toPx() + 2.dp.toPx(), center = center)
                drawCircle(color = color, radius = radius.toPx(), center = center)
            }

            if (scrubIndex >= 0) {
                val i = scrubIndex
                drawLine(
                    color = textColor.copy(alpha = 0.5f), start = Offset(x(i), 0f), end = Offset(x(i), size.height),
                    strokeWidth = 1.dp.toPx(),
                )
                marker(i, if (points[i].balance < 0) warnColor else lineColor, 6.dp)
            } else if (progress.value > 0.98f) {
                if (todayDay != null && todayDay in 1..points.size) marker(todayDay - 1, lineColor, 4.dp)
                if (lowest != null && lowest.day != todayDay) {
                    marker(lowest.day - 1, if (lowest.balance < 0) warnColor else lineColor.copy(alpha = 0.6f), 4.dp)
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf(1, 8, 15, 22, points.size).forEach { d ->
                Text(d.toString(), style = AppText.caption2, color = textColor)
            }
        }
    }
}

/** Тонкая полоса прогресса, которая «заполняется» при появлении. */
@Composable
fun AnimatedBar(
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
) {
    val c = BudgetTheme.colors
    var go by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { go = true }
    val f by animateFloatAsState(
        targetValue = if (go) fraction.coerceIn(0f, 1f) else 0f,
        animationSpec = tween(700, delayMillis = delayMillis, easing = IosEasing),
        label = "bar",
    )
    Box(modifier.height(6.dp).clip(CircleShape).background(c.fill)) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(f.coerceAtLeast(if (fraction > 0f) 0.03f else 0f))
                .clip(CircleShape)
                .background(color),
        )
    }
}
