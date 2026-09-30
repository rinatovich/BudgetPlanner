package com.budgetplanner.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.budgetplanner.app.ui.components.AppCard
import com.budgetplanner.app.ui.components.CategoryAvatar
import com.budgetplanner.app.ui.components.ConfirmDialog
import com.budgetplanner.app.ui.components.SecondaryText
import com.budgetplanner.app.ui.components.SectionTitle
import com.budgetplanner.app.ui.components.SegmentedChoice
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
    padding: PaddingValues,
    onTheme: (ThemeMode) -> Unit,
    onFirstDay: (DayOfWeek) -> Unit,
    onLoadDemo: () -> Unit,
    onClearData: () -> Unit,
    onDeleteCategory: (Category) -> Unit,
    onMessage: (String) -> Unit,
) {
    var pendingAction by remember { mutableStateOf<DataAction?>(null) }
    var pendingCategory by remember { mutableStateOf<Category?>(null) }

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = padding.calculateTopPadding() + 16.dp,
            bottom = padding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { Text("Настройки", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(horizontal = 4.dp)) }

        item {
            AppCard {
                SectionTitle("Тема")
                Spacer(Modifier.height(12.dp))
                SegmentedChoice(
                    options = listOf("Системная", "Светлая", "Тёмная"),
                    selectedIndex = ThemeMode.entries.indexOf(settings.theme),
                    onSelect = { onTheme(ThemeMode.entries[it]) },
                )
            }
        }

        item {
            AppCard {
                SectionTitle("Первый день недели")
                Spacer(Modifier.height(12.dp))
                SegmentedChoice(
                    options = listOf("Понедельник", "Воскресенье"),
                    selectedIndex = if (settings.firstDayOfWeek == DayOfWeek.SUNDAY) 1 else 0,
                    onSelect = { onFirstDay(if (it == 1) DayOfWeek.SUNDAY else DayOfWeek.MONDAY) },
                )
            }
        }

        item {
            AppCard {
                SectionTitle("Валюта")
                Spacer(Modifier.height(4.dp))
                Text("Узбекский сум (${settings.currency.code})", style = MaterialTheme.typography.bodyLarge)
                SecondaryText("Другие валюты появятся в следующих версиях.")
            }
        }

        item {
            AppCard {
                SectionTitle("Свои категории")
                Spacer(Modifier.height(4.dp))
                if (customCategories.isEmpty()) {
                    SecondaryText("Пока нет. Новую категорию можно создать при добавлении операции.")
                } else {
                    customCategories.forEach { category ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            CategoryAvatar(category.icon, size = 36.dp)
                            Spacer(Modifier.width(12.dp))
                            Text(category.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                            SecondaryText(if (category.type == EntryType.INCOME) "доход" else "расход")
                            IconButton(onClick = { pendingCategory = category }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Удалить категорию ${category.name}")
                            }
                        }
                    }
                }
            }
        }

        item {
            AppCard {
                SectionTitle("Данные")
                Spacer(Modifier.height(4.dp))
                SecondaryText("Демо-данные помогут быстро увидеть, как работает приложение.")
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { pendingAction = DataAction.DEMO }, modifier = Modifier.fillMaxWidth()) {
                    Text("Загрузить демо-данные")
                }
                TextButton(onClick = { pendingAction = DataAction.CLEAR }, modifier = Modifier.fillMaxWidth()) {
                    Text("Удалить все данные", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        item {
            AppCard {
                SectionTitle("О приложении")
                Spacer(Modifier.height(4.dp))
                Text("Бюджет · версия 1.0.0", style = MaterialTheme.typography.bodyLarge)
                SecondaryText("Работает без интернета. Все данные хранятся только на этом устройстве.")
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
