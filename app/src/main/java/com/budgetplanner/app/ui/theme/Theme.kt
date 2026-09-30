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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budgetplanner.domain.model.ThemeMode

/** Дополнительные цвета, которых нет в Material 3: доход и палитра графиков. */
@Immutable
data class ExtraColors(
    val income: Color,
    val chart: List<Color>,
    val essential: Color,
    val nonEssential: Color,
)

private val LightExtra = ExtraColors(
    income = Color(0xFF2E7D5B),
    chart = listOf(
        Color(0xFF2F6F6B), Color(0xFF5B9C96), Color(0xFF8FBFB9), Color(0xFF6B7A8F),
        Color(0xFF9AA7B8), Color(0xFFC5B8A8), Color(0xFFB0B7B5),
    ),
    essential = Color(0xFF2F6F6B),
    nonEssential = Color(0xFFA9B4C2),
)

private val DarkExtra = ExtraColors(
    income = Color(0xFF6FCF97),
    chart = listOf(
        Color(0xFF7CC4BD), Color(0xFF569D96), Color(0xFF3B7772), Color(0xFF8E9DB3),
        Color(0xFF66748A), Color(0xFFA89C8D), Color(0xFF7E8684),
    ),
    essential = Color(0xFF7CC4BD),
    nonEssential = Color(0xFF5B6A7E),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF2F6F6B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDCEBE9),
    onPrimaryContainer = Color(0xFF17403D),
    secondaryContainer = Color(0xFFDCEBE9),
    onSecondaryContainer = Color(0xFF17403D),
    background = Color(0xFFF6F6F3),
    onBackground = Color(0xFF1B1F1E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1B1F1E),
    surfaceVariant = Color(0xFFECEDEA),
    onSurfaceVariant = Color(0xFF636B69),
    outline = Color(0xFFB9BEBC),
    outlineVariant = Color(0xFFE3E5E2),
    error = Color(0xFFB3423A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF7E1DF),
    onErrorContainer = Color(0xFF6B1D17),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7CC4BD),
    onPrimary = Color(0xFF0B2A28),
    primaryContainer = Color(0xFF1F3D3A),
    onPrimaryContainer = Color(0xFFCDE8E5),
    secondaryContainer = Color(0xFF1F3D3A),
    onSecondaryContainer = Color(0xFFCDE8E5),
    background = Color(0xFF0F1211),
    onBackground = Color(0xFFE6EAE8),
    surface = Color(0xFF1A1E1D),
    onSurface = Color(0xFFE6EAE8),
    surfaceVariant = Color(0xFF262B2A),
    onSurfaceVariant = Color(0xFFA3ABA8),
    outline = Color(0xFF4A5250),
    outlineVariant = Color(0xFF2D3332),
    error = Color(0xFFF2A29B),
    onError = Color(0xFF4A120E),
    errorContainer = Color(0xFF5A2621),
    onErrorContainer = Color(0xFFF9DAD7),
)

private val baseTypography = Typography()

private val AppTypography = baseTypography.copy(
    headlineSmall = baseTypography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = baseTypography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = baseTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/** Крупные стили для сумм. */
object MoneyStyles {
    val hero = TextStyle(fontSize = 40.sp, lineHeight = 46.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-1).sp)
    val large = TextStyle(fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp)
    val medium = TextStyle(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold)
}

private val LocalExtraColors = staticCompositionLocalOf { LightExtra }

object BudgetTheme {
    val extra: ExtraColors
        @Composable
        @ReadOnlyComposable
        get() = LocalExtraColors.current
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
                navigationBarStyle = SystemBarStyle.auto(
                    AndroidColor.argb(0xe6, 0xFF, 0xFF, 0xFF),
                    AndroidColor.argb(0x80, 0x1b, 0x1b, 0x1b),
                ) { dark },
            )
            onDispose { }
        }
    }

    CompositionLocalProvider(LocalExtraColors provides (if (dark) DarkExtra else LightExtra)) {
        MaterialTheme(
            colorScheme = if (dark) DarkColors else LightColors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
