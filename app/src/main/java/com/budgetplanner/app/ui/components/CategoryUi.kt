package com.budgetplanner.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.FamilyRestroom
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.HomeWork
import androidx.compose.material.icons.rounded.Label
import androidx.compose.material.icons.rounded.Laptop
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.budgetplanner.app.ui.theme.AppText
import com.budgetplanner.app.ui.theme.BudgetTheme
import com.budgetplanner.domain.model.Category

fun categoryIcon(key: String): ImageVector = when (key) {
    "cart" -> Icons.Rounded.ShoppingCart
    "home" -> Icons.Rounded.Home
    "utilities" -> Icons.Rounded.Bolt
    "transport" -> Icons.Rounded.DirectionsBus
    "health" -> Icons.Rounded.HealthAndSafety
    "clothes" -> Icons.Rounded.Checkroom
    "education" -> Icons.Rounded.School
    "fun" -> Icons.Rounded.Movie
    "cafe" -> Icons.Rounded.Restaurant
    "credit" -> Icons.Rounded.CreditCard
    "internet" -> Icons.Rounded.Wifi
    "subscriptions" -> Icons.Rounded.Subscriptions
    "family" -> Icons.Rounded.FamilyRestroom
    "shopping" -> Icons.Rounded.ShoppingBag
    "salary" -> Icons.Rounded.Payments
    "side" -> Icons.Rounded.Work
    "bonus" -> Icons.Rounded.CardGiftcard
    "rent" -> Icons.Rounded.HomeWork
    "freelance" -> Icons.Rounded.Laptop
    "label" -> Icons.Rounded.Label
    else -> Icons.Rounded.MoreHoriz
}

/** Каждой категории — свой системный цвет Apple: список читается с одного взгляда. */
fun categoryColor(key: String): Color = when (key) {
    "cart" -> Color(0xFF34C759)
    "home" -> Color(0xFFFF9500)
    "utilities" -> Color(0xFFFFCC00)
    "transport" -> Color(0xFF5AC8FA)
    "health" -> Color(0xFFFF3B30)
    "clothes" -> Color(0xFFAF52DE)
    "education" -> Color(0xFF007AFF)
    "fun" -> Color(0xFFFF2D55)
    "cafe" -> Color(0xFFA2845E)
    "credit" -> Color(0xFF8E8E93)
    "internet" -> Color(0xFF32ADE6)
    "subscriptions" -> Color(0xFF5856D6)
    "family" -> Color(0xFFFF6482)
    "shopping" -> Color(0xFF30B0C7)
    "salary" -> Color(0xFF34C759)
    "side" -> Color(0xFF00C7BE)
    "bonus" -> Color(0xFFFFB800)
    "rent" -> Color(0xFF5856D6)
    "freelance" -> Color(0xFF007AFF)
    "label" -> Color(0xFF64748B)
    else -> Color(0xFF8E8E93)
}

/** Иконка категории: цветной «сквиркл» с белым глифом, как иконки в Настройках iOS. */
@Composable
fun CategoryAvatar(iconKey: String, modifier: Modifier = Modifier, size: Dp = 34.dp) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.26f))
            .background(categoryColor(iconKey)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = categoryIcon(iconKey),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(size * 0.58f),
        )
    }
}

/** Крупная иконка с подписью для горизонтальной ленты выбора категории. */
@Composable
fun CategoryBubble(
    name: String,
    iconKey: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = BudgetTheme.colors
    val ring by animateFloatAsState(if (selected) 1f else 0f, spring(dampingRatio = 0.7f, stiffness = 500f), label = "ring")
    Column(
        modifier = modifier.width(78.dp).bouncyClickable(scale = 0.92f, tick = true, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(62.dp)
                .border(2.5.dp, c.blue.copy(alpha = ring.coerceIn(0f, 1f)), RoundedCornerShape(19.dp)),
            contentAlignment = Alignment.Center,
        ) {
            CategoryAvatar(iconKey, size = 52.dp)
        }
        Text(
            text = name,
            style = AppText.caption1,
            color = if (selected) c.blue else c.secondaryLabel,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
fun NewCategoryBubble(onClick: () -> Unit, label: String = "Новая") {
    val c = BudgetTheme.colors
    Column(
        Modifier.width(78.dp).bouncyClickable(scale = 0.92f, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(62.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(c.fill),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, tint = c.blue, modifier = Modifier.size(26.dp))
            }
        }
        Text(label, style = AppText.caption1, color = c.blue, modifier = Modifier.padding(top = 4.dp), maxLines = 1)
    }
}

/** Горизонтальная лента категорий с «Новой» в конце. */
@Composable
fun CategoryStrip(
    categories: List<Category>,
    selectedId: Long?,
    onSelect: (Category) -> Unit,
    onNew: () -> Unit,
    newLabel: String = "Новая",
) {
    androidx.compose.foundation.lazy.LazyRow(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = SheetHorizontal - 4.dp),
    ) {
        items(categories.size, key = { categories[it].id }) { i ->
            val category = categories[i]
            CategoryBubble(category.name, category.icon, category.id == selectedId, onClick = { onSelect(category) })
        }
        item(key = "new-category") { NewCategoryBubble(onClick = onNew, label = newLabel) }
    }
}
