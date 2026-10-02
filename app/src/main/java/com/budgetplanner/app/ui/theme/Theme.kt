package com.budgetplanner.app.ui.theme

import android.graphics.Color as AndroidColor
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budgetplanner.app.R
import com.budgetplanner.domain.model.ThemeMode

/**
 * Палитра в духе iOS: системные цвета Apple (grouped background, label, separator, fill).
 * Все цвета приложения берутся отсюда через [BudgetTheme.colors].
 */
@Immutable
data class IosColors(
    val isDark: Boolean,
    /** systemGroupedBackground — фон экранов. */
    val background: Color,
    /** secondarySystemGroupedBackground — фон «ячеек» и карточек. */
    val cell: Color,
    val cellPressed: Color,
    /** Алерты, тосты, всплывающие элементы. */
    val elevated: Color,
    val label: Color,
    val secondaryLabel: Color,
    val tertiaryLabel: Color,
    val separator: Color,
    /** tertiarySystemFill — фон сегментов, кнопок-«таблеток». */
    val fill: Color,
    val segmentThumb: Color,
    val switchOff: Color,
    /** Полупрозрачный фон навбара и таббара. */
    val bar: Color,
    val blue: Color,
    val green: Color,
    val red: Color,
    val orange: Color,
)

private val LightIos = IosColors(
    isDark = false,
    background = Color(0xFFF2F2F7),
    cell = Color(0xFFFFFFFF),
    cellPressed = Color(0xFFE5E5EA),
    elevated = Color(0xFFFFFFFF),
    label = Color(0xFF000000),
    secondaryLabel = Color(0x993C3C43),
    tertiaryLabel = Color(0x4D3C3C43),
    separator = Color(0x4A3C3C43),
    fill = Color(0x1F787880),
    segmentThumb = Color(0xFFFFFFFF),
    switchOff = Color(0xFFE9E9EA),
    bar = Color(0xE8FFFFFF),
    blue = Color(0xFF007AFF),
    green = Color(0xFF34C759),
    red = Color(0xFFFF3B30),
    orange = Color(0xFFFF9500),
)

private val DarkIos = IosColors(
    isDark = true,
    background = Color(0xFF000000),
    cell = Color(0xFF1C1C1E),
    cellPressed = Color(0xFF2C2C2E),
    elevated = Color(0xFF2C2C2E),
    label = Color(0xFFFFFFFF),
    secondaryLabel = Color(0x99EBEBF5),
    tertiaryLabel = Color(0x4DEBEBF5),
    separator = Color(0xA6545458),
    fill = Color(0x3D767680),
    segmentThumb = Color(0xFF636366),
    switchOff = Color(0xFF39393D),
    bar = Color(0xE81C1C1E),
    blue = Color(0xFF0A84FF),
    green = Color(0xFF30D158),
    red = Color(0xFFFF453A),
    orange = Color(0xFFFF9F0A),
)

/**
 * Inter — открытый шрифт, ближайший аналог SF Pro (поддерживает кириллицу и табличные цифры).
 * Чтобы вернуть системный шрифт, замените на FontFamily.Default.
 */
val InterFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)

private fun style(size: Int, line: Int, weight: FontWeight, tracking: Double = 0.0) = TextStyle(
    fontFamily = InterFamily,
    fontSize = size.sp,
    lineHeight = line.sp,
    fontWeight = weight,
    letterSpacing = tracking.sp,
)

/** Текстовые стили iOS (Large Title, Headline, Body, Footnote …). */
object AppText {
    val largeTitle = style(34, 41, FontWeight.Bold, -0.7)
    val title1 = style(28, 34, FontWeight.Bold, -0.5)
    val title2 = style(22, 28, FontWeight.SemiBold, -0.35)
    val title3 = style(20, 25, FontWeight.SemiBold, -0.3)
    val headline = style(17, 22, FontWeight.SemiBold, -0.3)
    val body = style(17, 22, FontWeight.Normal, -0.3)
    val callout = style(16, 21, FontWeight.Normal, -0.25)
    val subhead = style(15, 20, FontWeight.Normal, -0.2)
    val footnote = style(13, 18, FontWeight.Normal, -0.1)
    val caption1 = style(12, 16, FontWeight.Normal, 0.0)
    val caption2 = style(11, 13, FontWeight.Medium, 0.05)
}

/** Стили для денег: табличные цифры не «прыгают» при анимации чисел. */
object MoneyStyles {
    private const val TABULAR = "tnum"

    val hero = TextStyle(
        fontFamily = InterFamily, fontSize = 46.sp, lineHeight = 52.sp,
        fontWeight = FontWeight.Bold, letterSpacing = (-1.4).sp, fontFeatureSettings = TABULAR,
    )
    val input = TextStyle(
        fontFamily = InterFamily, fontSize = 48.sp, lineHeight = 56.sp,
        fontWeight = FontWeight.Bold, letterSpacing = (-1.2).sp, fontFeatureSettings = TABULAR,
    )
    val unit = TextStyle(
        fontFamily = InterFamily, fontSize = 22.sp, lineHeight = 28.sp,
        fontWeight = FontWeight.SemiBold, letterSpacing = (-0.3).sp,
    )
    val large = TextStyle(
        fontFamily = InterFamily, fontSize = 28.sp, lineHeight = 34.sp,
        fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp, fontFeatureSettings = TABULAR,
    )
    val medium = TextStyle(
        fontFamily = InterFamily, fontSize = 20.sp, lineHeight = 25.sp,
        fontWeight = FontWeight.SemiBold, letterSpacing = (-0.3).sp, fontFeatureSettings = TABULAR,
    )
    val row = TextStyle(
        fontFamily = InterFamily, fontSize = 17.sp, lineHeight = 22.sp,
        fontWeight = FontWeight.SemiBold, letterSpacing = (-0.3).sp, fontFeatureSettings = TABULAR,
    )
}

/** Скругления: карточки iOS чуть крупнее классических — как в новых версиях системы. */
val CardShape = RoundedCornerShape(18.dp)
val SheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

private val AppTypography = Typography(
    displaySmall = AppText.largeTitle,
    headlineLarge = AppText.title1,
    headlineMedium = AppText.title1,
    headlineSmall = AppText.title2,
    titleLarge = AppText.title2,
    titleMedium = AppText.headline,
    titleSmall = AppText.subhead.copy(fontWeight = FontWeight.SemiBold),
    bodyLarge = AppText.body,
    bodyMedium = AppText.subhead,
    bodySmall = AppText.footnote,
    labelLarge = AppText.subhead.copy(fontWeight = FontWeight.Medium),
    labelMedium = AppText.footnote,
    labelSmall = AppText.caption2,
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

private fun materialLight(c: IosColors) = lightColorScheme(
    primary = c.blue,
    onPrimary = Color.White,
    primaryContainer = c.blue.copy(alpha = 0.14f),
    onPrimaryContainer = c.blue,
    secondaryContainer = c.fill,
    onSecondaryContainer = c.label,
    background = c.background,
    onBackground = c.label,
    surface = c.cell,
    onSurface = c.label,
    surfaceVariant = Color(0xFFE5E5EA),
    onSurfaceVariant = c.secondaryLabel,
    outline = c.separator,
    outlineVariant = c.separator,
    error = c.red,
    onError = Color.White,
    errorContainer = c.red.copy(alpha = 0.14f),
    onErrorContainer = c.red,
    surfaceTint = Color.Transparent,
)

private fun materialDark(c: IosColors) = darkColorScheme(
    primary = c.blue,
    onPrimary = Color.White,
    primaryContainer = c.blue.copy(alpha = 0.22f),
    onPrimaryContainer = c.blue,
    secondaryContainer = c.fill,
    onSecondaryContainer = c.label,
    background = c.background,
    onBackground = c.label,
    surface = c.cell,
    onSurface = c.label,
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = c.secondaryLabel,
    outline = c.separator,
    outlineVariant = c.separator,
    error = c.red,
    onError = Color.White,
    errorContainer = c.red.copy(alpha = 0.22f),
    onErrorContainer = c.red,
    surfaceTint = Color.Transparent,
)

private val LocalIosColors = staticCompositionLocalOf { LightIos }

object BudgetTheme {
    val colors: IosColors
        @Composable
        @ReadOnlyComposable
        get() = LocalIosColors.current
}

@Composable
fun BudgetTheme(themeMode: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        val activity = LocalContext.current as? ComponentActivity
        DisposableEffect(dark) {
            activity?.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.auto(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT) { dark },
                navigationBarStyle = SystemBarStyle.auto(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT) { dark },
            )
            onDispose { }
        }
    }

    val ios = if (dark) DarkIos else LightIos
    CompositionLocalProvider(LocalIosColors provides ios) {
        MaterialTheme(
            colorScheme = if (dark) materialDark(ios) else materialLight(ios),
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
