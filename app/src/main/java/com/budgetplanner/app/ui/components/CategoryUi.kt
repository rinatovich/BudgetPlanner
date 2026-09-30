@file:OptIn(ExperimentalMaterial3Api::class)

package com.budgetplanner.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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

@Composable
fun CategoryAvatar(iconKey: String, modifier: Modifier = Modifier, size: Dp = 44.dp) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = categoryIcon(iconKey),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(size * 0.5f),
        )
    }
}

@Composable
fun CategoryChip(
    category: Category,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        label = { Text(category.name, maxLines = 1) },
        leadingIcon = {
            Icon(
                imageVector = categoryIcon(category.icon),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        },
    )
}
