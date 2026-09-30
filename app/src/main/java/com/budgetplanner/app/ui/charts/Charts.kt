package com.budgetplanner.app.ui.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.budgetplanner.domain.calc.DailyBalance
import com.budgetplanner.domain.calc.MoneyFormat
import kotlin.math.abs
import kotlin.math.max

data class DonutSlice(val value: Long, val color: Color)

/** Кольцевая диаграмма. В центре — произвольное содержимое. */
@Composable
fun DonutChart(
    slices: List<DonutSlice>,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 28.dp,
    center: @Composable () -> Unit = {},
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(slices) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(durationMillis = 800, easing = FastOutSlowInEasing))
    }
    val track = MaterialTheme.colorScheme.surfaceVariant

    Box(modifier = modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val diameter = size.minDimension - stroke
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)
            val total = slices.sumOf { it.value }.toFloat()

            drawArc(
                color = track, startAngle = 0f, sweepAngle = 360f, useCenter = false,
                topLeft = topLeft, size = arcSize, style = Stroke(width = stroke),
            )
            if (total > 0f) {
                val gap = if (slices.size > 1) 2f else 0f
                var start = -90f
                slices.forEach { slice ->
                    val sweep = slice.value / total * 360f * progress.value
                    val drawn = (sweep - gap).coerceAtLeast(0f)
                    if (drawn > 0f) {
                        drawArc(
                            color = slice.color, startAngle = start + gap / 2f, sweepAngle = drawn,
                            useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(width = stroke),
                        )
                    }
                    start += sweep
                }
            }
        }
        center()
    }
}

/** Одна горизонтальная полоса сравнения: подпись, полоса, значение. */
@Composable
fun BarRow(
    label: String,
    value: Long,
    maxValue: Long,
    color: Color,
    valueText: String,
    modifier: Modifier = Modifier,
) {
    val target = if (maxValue > 0) (abs(value).toFloat() / maxValue).coerceIn(0f, 1f) else 0f
    val fraction by animateFloatAsState(target, tween(600, easing = FastOutSlowInEasing), label = "bar")
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(84.dp),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction.coerceAtLeast(if (value != 0L) 0.02f else 0f))
                    .clip(RoundedCornerShape(6.dp))
                    .background(color),
            )
        }
        Text(
            valueText,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.width(72.dp).padding(start = 8.dp),
            maxLines = 1,
        )
    }
}

/** Двухсегментная полоса: обязательные / необязательные расходы. */
@Composable
fun SplitBar(first: Long, second: Long, firstColor: Color, secondColor: Color, modifier: Modifier = Modifier) {
    val total = (first + second).coerceAtLeast(1)
    val target = first.toFloat() / total
    val fraction by animateFloatAsState(target, tween(600, easing = FastOutSlowInEasing), label = "split")
    Row(modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(7.dp)).background(secondColor)) {
        if (first > 0) {
            Box(Modifier.fillMaxHeight().fillMaxWidth(fraction).background(firstColor))
        }
    }
}

/** График накопленного баланса по дням месяца с отметкой минимума. */
@Composable
fun BalanceLineChart(
    points: List<DailyBalance>,
    lowest: DailyBalance?,
    todayDay: Int?,
    modifier: Modifier = Modifier,
) {
    if (points.size < 2) return
    val progress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(durationMillis = 900, easing = FastOutSlowInEasing))
    }
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val warnColor = MaterialTheme.colorScheme.error
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    val maxV = max(points.maxOf { it.balance }, 0L)
    val minV = minOf(points.minOf { it.balance }, 0L)

    Column(modifier) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(MoneyFormat.compact(maxV), style = MaterialTheme.typography.labelSmall, color = textColor)
            if (minV < 0) {
                Text(MoneyFormat.compact(minV), style = MaterialTheme.typography.labelSmall, color = textColor)
            }
        }
        Canvas(Modifier.fillMaxWidth().height(150.dp)) {
            val padV = 8.dp.toPx()
            val h = size.height - padV * 2
            val w = size.width
            val range = (maxV - minV).toFloat().coerceAtLeast(1f)
            fun x(i: Int) = w * i / (points.size - 1)
            fun y(v: Long) = padV + h * (1f - (v - minV).toFloat() / range)

            // нулевая линия
            drawLine(
                color = gridColor, start = Offset(0f, y(0)), end = Offset(w, y(0)),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)),
            )

            val line = Path()
            points.forEachIndexed { i, p ->
                if (i == 0) line.moveTo(x(i), y(p.balance)) else line.lineTo(x(i), y(p.balance))
            }
            val area = Path().apply {
                addPath(line)
                lineTo(w, y(0))
                lineTo(0f, y(0))
                close()
            }

            clipRect(left = 0f, top = 0f, right = w * progress.value, bottom = size.height) {
                drawPath(area, Brush.verticalGradient(listOf(lineColor.copy(alpha = 0.18f), Color.Transparent)))
                drawPath(
                    line, color = lineColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
            }

            if (todayDay != null && todayDay in 1..points.size) {
                val i = todayDay - 1
                drawLine(
                    color = textColor.copy(alpha = 0.5f), start = Offset(x(i), 0f), end = Offset(x(i), size.height),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            if (lowest != null && progress.value > 0.98f) {
                val i = lowest.day - 1
                val c = if (lowest.balance < 0) warnColor else lineColor
                drawCircle(color = c, radius = 6.dp.toPx(), center = Offset(x(i), y(lowest.balance)))
                drawCircle(color = Color.White, radius = 2.5.dp.toPx(), center = Offset(x(i), y(lowest.balance)))
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val last = points.size
            listOf(1, 8, 15, 22, last).forEach { d ->
                Text(d.toString(), style = MaterialTheme.typography.labelSmall, color = textColor)
            }
        }
    }
}
