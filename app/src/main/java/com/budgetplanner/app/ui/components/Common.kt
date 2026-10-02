@file:OptIn(ExperimentalMaterial3Api::class)

package com.budgetplanner.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.budgetplanner.app.ui.theme.AppText
import com.budgetplanner.app.ui.theme.BudgetTheme
import com.budgetplanner.app.ui.theme.CardShape
import com.budgetplanner.app.ui.theme.SheetShape
import com.budgetplanner.domain.calc.MoneyFormat
import com.budgetplanner.domain.model.Currency
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset

/** Отступ содержимого шторок от краёв. */
val SheetHorizontal = 20.dp

/** Высота, которую занимает плавающий таб-бар: контент прокручивается под ним. */
private val TabBarClearance = 112.dp

/* -------------------------------------------------------------------------- */
/*  Экран с большим заголовком                                                 */
/* -------------------------------------------------------------------------- */

/**
 * Каркас экрана в стиле iOS: большой заголовок, который при прокрутке «сворачивается»
 * в компактный навбар с полупрозрачным фоном и тонкой линией.
 */
@Composable
fun LargeTitleScreen(
    title: String,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    titleAccessory: (@Composable () -> Unit)? = null,
    hasTabBar: Boolean = true,
    contentSpacing: Dp = 0.dp,
    listState: LazyListState = rememberLazyListState(),
    content: LazyListScope.() -> Unit,
) {
    val c = BudgetTheme.colors
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val barHeight = 44.dp
    val thresholdPx = with(LocalDensity.current) { 36.dp.toPx() }

    val collapsed by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > thresholdPx }
    }
    val barAlpha by animateFloatAsState(if (collapsed) 1f else 0f, tween(200), label = "navbar-alpha")

    Box(modifier.fillMaxSize().background(c.background)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = statusTop + barHeight,
                bottom = navBottom + if (hasTabBar) TabBarClearance else 32.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(contentSpacing),
        ) {
            item(key = "large-title") {
                Row(
                    Modifier.fillMaxWidth().padding(start = 4.dp, top = 2.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        style = AppText.largeTitle,
                        color = c.label,
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                    )
                    titleAccessory?.invoke()
                }
            }
            content()
        }

        // Навбар поверх списка.
        Box(Modifier.fillMaxWidth().height(statusTop + barHeight).align(Alignment.TopCenter)) {
            Box(Modifier.matchParentSize().graphicsLayer { alpha = barAlpha }.background(c.bar))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .align(Alignment.BottomCenter)
                    .graphicsLayer { alpha = barAlpha }
                    .background(c.separator),
            )
            Row(
                Modifier.fillMaxSize().padding(top = statusTop).padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) { leading?.invoke() }
                Text(
                    text = title,
                    style = AppText.headline,
                    color = c.label,
                    maxLines = 1,
                    modifier = Modifier.graphicsLayer { alpha = barAlpha },
                )
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) { trailing?.invoke() }
            }
        }
    }
}

/** Кнопка-иконка в навбаре (шестерёнка, «+» и т. п.). */
@Composable
fun NavIconButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).bouncyClickable(scale = 0.88f, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = BudgetTheme.colors.blue, modifier = Modifier.size(24.dp))
    }
}

/** «‹ Назад» в навбаре. */
@Composable
fun NavBackButton(label: String, onClick: () -> Unit) {
    val c = BudgetTheme.colors
    Row(
        Modifier.height(44.dp).bouncyClickable(scale = 0.94f, onClick = onClick).padding(end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.ChevronLeft, contentDescription = null, tint = c.blue, modifier = Modifier.size(30.dp))
        Text(label, style = AppText.body, color = c.blue)
    }
}

/* -------------------------------------------------------------------------- */
/*  Группы и заголовки секций                                                  */
/* -------------------------------------------------------------------------- */

/** Скруглённая «группа» (inset grouped) — контейнер для строк. */
@Composable
fun GroupCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier.fillMaxWidth().clip(CardShape).background(BudgetTheme.colors.cell),
        content = content,
    )
}

/** Тонкий разделитель внутри группы с отступом слева. */
@Composable
fun RowDivider(start: Dp = 16.dp) {
    HorizontalDivider(
        modifier = Modifier.padding(start = start),
        thickness = 0.5.dp,
        color = BudgetTheme.colors.separator,
    )
}

/** Форма строки внутри LazyColumn: скругляются только верх первой и низ последней. */
fun groupShape(index: Int, count: Int, radius: Dp = 18.dp): RoundedCornerShape {
    val top = if (index == 0) radius else 0.dp
    val bottom = if (index == count - 1) radius else 0.dp
    return RoundedCornerShape(topStart = top, topEnd = top, bottomEnd = bottom, bottomStart = bottom)
}

/** Заголовок секции: мелкие заглавные буквы, как в Настройках iOS. */
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, trailing: String? = null) {
    val c = BudgetTheme.colors
    Row(
        modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(title.uppercase(), style = AppText.footnote, color = c.secondaryLabel, maxLines = 1)
        if (trailing != null) {
            Text(
                text = trailing,
                style = AppText.footnote.copy(fontFeatureSettings = "tnum"),
                color = c.secondaryLabel,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Пояснение под группой. */
@Composable
fun SectionFooter(text: String, modifier: Modifier = Modifier, color: Color = BudgetTheme.colors.secondaryLabel) {
    Text(
        text = text,
        style = AppText.footnote,
        color = color,
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text = text, style = AppText.headline, color = BudgetTheme.colors.label, modifier = modifier)
}

/** Вторичный, приглушённый текст. */
@Composable
fun SecondaryText(text: String, modifier: Modifier = Modifier, textAlign: TextAlign? = null) {
    Text(
        text = text,
        style = AppText.subhead,
        color = BudgetTheme.colors.secondaryLabel,
        modifier = modifier,
        textAlign = textAlign,
    )
}

/* -------------------------------------------------------------------------- */
/*  Переключатель месяца                                                       */
/* -------------------------------------------------------------------------- */

@Composable
private fun CircleIconButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    val c = BudgetTheme.colors
    Box(
        Modifier
            .size(34.dp)
            .bouncyClickable(scale = 0.86f, tick = true, onClick = onClick)
            .clip(CircleShape)
            .background(c.fill),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = c.blue, modifier = Modifier.size(20.dp))
    }
}

/** Стрелки и название месяца; название «уезжает» в сторону перелистывания. */
@Composable
fun MonthSwitcher(
    month: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = BudgetTheme.colors
    Row(modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        CircleIconButton(Icons.Rounded.ChevronLeft, "Предыдущий месяц", onPrevious)
        AnimatedContent(
            targetState = month,
            modifier = Modifier.weight(1f),
            transitionSpec = {
                val dir = if (targetState > initialState) 1 else -1
                (slideInHorizontally(tween(340, easing = IosEasing)) { it * dir / 2 } + fadeIn(tween(240))) togetherWith
                    (slideOutHorizontally(tween(340, easing = IosEasing)) { -it * dir / 2 } + fadeOut(tween(140)))
            },
            label = "month-title",
        ) { m ->
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = monthTitle(m),
                    style = AppText.headline,
                    color = c.label,
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClickLabel = "Перейти к текущему месяцу",
                            onClick = onReset,
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }
        CircleIconButton(Icons.Rounded.ChevronRight, "Следующий месяц", onNext)
    }
}

/** Появляется рядом с заголовком, когда открыт не текущий месяц. */
@Composable
fun TodayChip(visible: Boolean, onClick: () -> Unit) {
    val c = BudgetTheme.colors
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(220)) + scaleIn(initialScale = 0.8f, animationSpec = spring(0.7f, 500f)),
        exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.8f),
    ) {
        Text(
            text = "Сегодня",
            style = AppText.subhead.copy(fontWeight = FontWeight.SemiBold),
            color = c.blue,
            modifier = Modifier
                .bouncyClickable(scale = 0.92f, tick = true, onClick = onClick)
                .clip(CircleShape)
                .background(c.blue.copy(alpha = 0.12f))
                .padding(horizontal = 14.dp, vertical = 7.dp),
        )
    }
}

/* -------------------------------------------------------------------------- */
/*  Числа, пустые состояния, загрузка                                          */
/* -------------------------------------------------------------------------- */

/** Плавно «докручивает» число до нового значения. */
@Composable
fun animatedLong(target: Long): Long {
    var from by remember { mutableLongStateOf(target) }
    var to by remember { mutableLongStateOf(target) }
    val progress = remember { Animatable(1f) }

    LaunchedEffect(target) {
        if (target != to) {
            val current = from + ((to - from) * progress.value.toDouble()).toLong()
            from = current
            to = target
            progress.snapTo(0f)
            progress.animateTo(1f, tween(durationMillis = 700, easing = FastOutSlowInEasing))
        }
    }
    return from + ((to - from) * progress.value.toDouble()).toLong()
}

fun formatMoney(value: Long, currency: Currency, showPlus: Boolean = false, withCurrency: Boolean = true): String =
    MoneyFormat.full(value, currency, showPlus, withCurrency)

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actions: @Composable ColumnScope.() -> Unit = {},
) {
    val c = BudgetTheme.colors
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) { appear.animateTo(1f, spring(dampingRatio = 0.7f, stiffness = 180f)) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 24.dp)
            .graphicsLayer {
                val s = 0.9f + 0.1f * appear.value
                scaleX = s
                scaleY = s
                alpha = appear.value.coerceIn(0f, 1f)
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(88.dp).clip(RoundedCornerShape(26.dp)).background(c.blue.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = c.blue, modifier = Modifier.size(44.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text(title, style = AppText.title3, color = c.label, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, style = AppText.subhead, color = c.secondaryLabel, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        actions()
    }
}

/** Классический iOS-спиннер из 12 лучей. */
@Composable
fun IosSpinner(modifier: Modifier = Modifier, diameter: Dp = 22.dp) {
    val transition = rememberInfiniteTransition(label = "spinner")
    val step by transition.animateFloat(
        initialValue = 0f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing)),
        label = "spinner-step",
    )
    val color = BudgetTheme.colors.secondaryLabel
    Canvas(modifier.size(diameter)) {
        val current = step.toInt() % 12
        val r = this.size.minDimension / 2f
        for (i in 0 until 12) {
            val age = (current - i + 12) % 12
            rotate(degrees = i * 30f) {
                drawLine(
                    color = color.copy(alpha = 1f - age / 12f * 0.85f),
                    start = Offset(r, r * 0.06f),
                    end = Offset(r, r * 0.36f),
                    strokeWidth = r * 0.16f,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

@Composable
fun LoadingRow() {
    Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) { IosSpinner() }
}

/* -------------------------------------------------------------------------- */
/*  Шторки и выбор даты                                                        */
/* -------------------------------------------------------------------------- */

/**
 * Нижняя шторка. Содержимое не имеет боковых отступов — секции сами используют [SheetHorizontal],
 * поэтому горизонтальные ленты могут доходить до краёв экрана.
 * [content] получает функцию dismiss, которая плавно закрывает шторку.
 */
@Composable
fun AppBottomSheet(
    onDismiss: () -> Unit,
    grouped: Boolean = true,
    content: @Composable ColumnScope.(dismiss: () -> Unit) -> Unit,
) {
    val c = BudgetTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val dismiss: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = SheetShape,
        containerColor = if (grouped) c.background else c.cell,
        scrimColor = Color.Black.copy(alpha = 0.36f),
        dragHandle = {
            Box(
                Modifier.padding(top = 8.dp, bottom = 6.dp).size(width = 36.dp, height = 5.dp)
                    .clip(CircleShape).background(c.tertiaryLabel),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 20.dp),
        ) {
            content(dismiss)
        }
    }
}

@Composable
fun AppDatePickerDialog(initial: LocalDate, onPick: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val c = BudgetTheme.colors
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
    )
    val colors = DatePickerDefaults.colors(
        selectedDayContainerColor = c.blue,
        selectedDayContentColor = Color.White,
        todayContentColor = c.blue,
        todayDateBorderColor = c.blue,
    )
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(c.elevated)
                .padding(bottom = 8.dp),
        ) {
            DatePicker(state = state, colors = colors, showModeToggle = false)
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.End) {
                TextAction("Отмена", onClick = onDismiss, color = c.blue)
                TextAction(
                    text = "Готово",
                    bold = true,
                    onClick = {
                        val millis = state.selectedDateMillis
                        if (millis != null) {
                            onPick(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                        } else {
                            onDismiss()
                        }
                    },
                )
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/*  Контролы: сегменты, переключатель, кнопки                                  */
/* -------------------------------------------------------------------------- */

/** Сегментированный контрол iOS: «ползунок» плавно переезжает на выбранный вариант. */
@Composable
fun SegmentedChoice(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = BudgetTheme.colors
    val tick = rememberTick()
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(c.fill)
            .padding(2.dp),
    ) {
        val segment = maxWidth / options.size
        val offset by animateDpAsState(
            targetValue = segment * selectedIndex,
            animationSpec = spring(dampingRatio = 0.8f, stiffness = 520f),
            label = "segment-offset",
        )
        Box(
            Modifier
                .offset(x = offset)
                .width(segment)
                .fillMaxHeight()
                .shadow(2.dp, RoundedCornerShape(9.dp), clip = false)
                .clip(RoundedCornerShape(9.dp))
                .background(c.segmentThumb),
        )
        Row(Modifier.fillMaxSize()) {
            options.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.RadioButton,
                        ) {
                            if (!selected) {
                                tick()
                                onSelect(index)
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = AppText.subhead.copy(
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        ),
                        color = c.label,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/** Переключатель в стиле iOS (зелёный, с пружинным движением ползунка). */
@Composable
fun IosSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val c = BudgetTheme.colors
    val tick = rememberTick()
    val track by animateColorAsState(if (checked) c.green else c.switchOff, tween(220), label = "switch-track")
    val thumbX by animateDpAsState(
        targetValue = if (checked) 20.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 600f),
        label = "switch-thumb",
    )
    Box(
        modifier
            .size(width = 51.dp, height = 31.dp)
            .clip(CircleShape)
            .background(track)
            .toggleable(
                value = checked,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Switch,
            ) {
                tick()
                onCheckedChange(it)
            }
            .padding(2.dp),
    ) {
        Box(
            Modifier
                .offset(x = thumbX)
                .size(27.dp)
                .shadow(3.dp, CircleShape, clip = false)
                .background(Color.White, CircleShape),
        )
    }
}

/** Главная кнопка: синяя «таблетка» на всю ширину. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = BudgetTheme.colors.blue,
) {
    val c = BudgetTheme.colors
    Box(
        modifier
            .fillMaxWidth()
            .height(52.dp)
            .bouncyClickable(scale = 0.97f, enabled = enabled, onClick = onClick)
            .clip(CircleShape)
            .background(if (enabled) color else c.fill),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = AppText.headline, color = if (enabled) Color.White else c.tertiaryLabel)
    }
}

/** Текстовая кнопка без фона. */
@Composable
fun TextAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = BudgetTheme.colors.blue,
    bold: Boolean = false,
    enabled: Boolean = true,
) {
    Text(
        text = text,
        style = if (bold) AppText.headline else AppText.body,
        color = if (enabled) color else color.copy(alpha = 0.4f),
        modifier = modifier
            .bouncyClickable(scale = 0.95f, enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    )
}

/* -------------------------------------------------------------------------- */
/*  Тост                                                                       */
/* -------------------------------------------------------------------------- */

data class ToastData(
    val id: Long,
    val text: String,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
)

/** Всплывающее уведомление-«капсула» сверху, вместо снэкбара Material. */
@Composable
fun ToastPill(toast: ToastData, onAction: () -> Unit) {
    val c = BudgetTheme.colors
    Row(
        Modifier
            .padding(horizontal = 16.dp)
            .shadow(18.dp, CircleShape, clip = false, ambientColor = Color.Black.copy(0.12f), spotColor = Color.Black.copy(0.2f))
            .background(c.elevated, CircleShape)
            .padding(start = 20.dp, end = if (toast.actionLabel != null) 8.dp else 20.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = toast.text,
            style = AppText.subhead,
            color = c.label,
            maxLines = 2,
            modifier = Modifier.weight(1f, fill = false).padding(vertical = 6.dp),
        )
        if (toast.actionLabel != null) {
            Spacer(Modifier.width(8.dp))
            Text(
                text = toast.actionLabel,
                style = AppText.subhead.copy(fontWeight = FontWeight.SemiBold),
                color = c.blue,
                modifier = Modifier
                    .bouncyClickable(scale = 0.92f, onClick = onAction)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            )
        }
    }
}
