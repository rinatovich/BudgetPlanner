package com.budgetplanner.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.budgetplanner.app.ui.components.CategoryAvatar
import com.budgetplanner.app.ui.components.ConfirmDialog
import com.budgetplanner.app.ui.components.GroupCard
import com.budgetplanner.app.ui.components.LargeTitleScreen
import com.budgetplanner.app.ui.components.NavBackButton
import com.budgetplanner.app.ui.components.RowDivider
import com.budgetplanner.app.ui.components.SectionFooter
import com.budgetplanner.app.ui.components.SectionHeader
import com.budgetplanner.app.ui.components.SegmentedChoice
import com.budgetplanner.app.ui.components.SwipeToDeleteBox
import com.budgetplanner.app.ui.components.groupShape
import com.budgetplanner.app.ui.components.rowClickable
import com.budgetplanner.app.ui.theme.AppText
import com.budgetplanner.app.ui.theme.BudgetTheme
import com.budgetplanner.domain.model.AppSettings
import com.budgetplanner.domain.model.Category
import com.budgetplanner.domain.model.EntryType
import com.budgetplanner.domain.model.ThemeMode
import java.time.DayOfWeek

private enum class DataAction { DEMO, CLEAR }

@Composable
fun SettingsScreen(
    settings: AppSettings,
    customCategories: List<Category>,
    onBack: () -> Unit,
    onTheme: (ThemeMode) -> Unit,
    onFirstDay: (DayOfWeek) -> Unit,
    onLoadDemo: () -> Unit,
    onClearData: () -> Unit,
    onDeleteCategory: (Category) -> Unit,
    onMessage: (String) -> Unit,
) {
    val c = BudgetTheme.colors
    var pendingAction by remember { mutableStateOf<DataAction?>(null) }
    var pendingCategory by remember { mutableStateOf<Category?>(null) }

    LargeTitleScreen(
        title = "Настройки",
        leading = { NavBackButton("Обзор", onBack) },
        hasTabBar = false,
    ) {
        item(key = "appearance") {
            Column {
                SectionHeader("Оформление")
                GroupCard {
                    Column(Modifier.padding(16.dp)) {
                        Text("Тема", style = AppText.body, color = c.label)
                        Spacer(Modifier.height(10.dp))
                        SegmentedChoice(
                            options = listOf("Системная", "Светлая", "Тёмная"),
                            selectedIndex = ThemeMode.entries.indexOf(settings.theme),
                            onSelect = { onTheme(ThemeMode.entries[it]) },
                        )
                    }
                }
            }
        }

        item(key = "calendar") {
            Column {
                SectionHeader("Календарь")
                GroupCard {
                    Column(Modifier.padding(16.dp)) {
                        Text("Первый день недели", style = AppText.body, color = c.label)
                        Spacer(Modifier.height(10.dp))
                        SegmentedChoice(
                            options = listOf("Понедельник", "Воскресенье"),
                            selectedIndex = if (settings.firstDayOfWeek == DayOfWeek.SUNDAY) 1 else 0,
                            onSelect = { onFirstDay(if (it == 1) DayOfWeek.SUNDAY else DayOfWeek.MONDAY) },
                        )
                    }
                    RowDivider()
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp)) {
                        Text("Валюта", style = AppText.body, color = c.label, modifier = Modifier.weight(1f))
                        Text("Узбекский сум (${settings.currency.code})", style = AppText.body, color = c.secondaryLabel)
                    }
                }
                SectionFooter("Другие валюты появятся в следующих версиях.")
            }
        }

        item(key = "categories-header") { SectionHeader("Свои категории") }
        if (customCategories.isEmpty()) {
            item(key = "categories-empty") {
                GroupCard {
                    Text(
                        "Пока нет. Новую категорию можно создать при добавлении операции.",
                        style = AppText.subhead,
                        color = c.secondaryLabel,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        } else {
            itemsIndexed(customCategories, key = { _, cat -> "cat-${cat.id}" }) { index, category ->
                SwipeToDeleteBox(
                    shape = groupShape(index, customCategories.size),
                    onDelete = { pendingCategory = category },
                    removeImmediately = false,
                ) {
                    Column {
                        Row(
                            Modifier.fillMaxWidth().background(c.cell).padding(horizontal = 16.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CategoryAvatar(category.icon)
                            Spacer(Modifier.width(12.dp))
                            Text(category.name, style = AppText.body, color = c.label, modifier = Modifier.weight(1f))
                            Text(
                                if (category.type == EntryType.INCOME) "доход" else "расход",
                                style = AppText.subhead,
                                color = c.secondaryLabel,
                            )
                        }
                        if (index < customCategories.lastIndex) RowDivider(start = 62.dp)
                    }
                }
            }
            item(key = "categories-hint") { SectionFooter("Смахните категорию влево, чтобы удалить.") }
        }

        item(key = "data") {
            Column {
                SectionHeader("Данные")
                GroupCard {
                    SettingsAction("Загрузить демо-данные", c.blue) { pendingAction = DataAction.DEMO }
                    RowDivider()
                    SettingsAction("Удалить все данные", c.red) { pendingAction = DataAction.CLEAR }
                }
                SectionFooter("Демо-данные помогут быстро увидеть, как работает приложение.")
            }
        }

        item(key = "about") {
            Column {
                SectionHeader("О приложении")
                GroupCard {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp)) {
                        Text("Версия", style = AppText.body, color = c.label, modifier = Modifier.weight(1f))
                        Text("1.0.0", style = AppText.body, color = c.secondaryLabel)
                    }
                }
                SectionFooter("Работает без интернета. Все данные хранятся только на этом устройстве.")
            }
        }
    }

    when (pendingAction) {
        DataAction.DEMO -> ConfirmDialog(
            title = "Загрузить демо-данные?",
            text = "К вашим данным добавятся примеры доходов, расходов и операций.",
            confirmLabel = "Загрузить",
            destructive = false,
            onConfirm = {
                pendingAction = null
                onLoadDemo()
                onMessage("Демо-данные загружены")
            },
            onDismiss = { pendingAction = null },
        )
        DataAction.CLEAR -> ConfirmDialog(
            title = "Удалить все данные?",
            text = "Будут удалены весь план и все операции. Это действие нельзя отменить.",
            confirmLabel = "Удалить",
            onConfirm = {
                pendingAction = null
                onClearData()
                onMessage("Данные удалены")
            },
            onDismiss = { pendingAction = null },
        )
        null -> Unit
    }

    val category = pendingCategory
    if (category != null) {
        ConfirmDialog(
            title = "Удалить категорию «${category.name}»?",
            text = "Её операции перейдут в категорию «Другое».",
            confirmLabel = "Удалить",
            onConfirm = {
                pendingCategory = null
                onDeleteCategory(category)
            },
            onDismiss = { pendingCategory = null },
        )
    }
}

@Composable
private fun SettingsAction(label: String, color: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().rowClickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 13.dp),
    ) {
        Text(label, style = AppText.body, color = color)
    }
}
