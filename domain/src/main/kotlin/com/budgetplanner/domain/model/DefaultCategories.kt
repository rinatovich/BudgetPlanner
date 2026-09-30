package com.budgetplanner.domain.model

object DefaultCategories {
    const val OTHER = "Другое"

    val expense: List<Category> = listOf(
        cat("Продукты", "cart", EntryType.EXPENSE, true),
        cat("Жильё", "home", EntryType.EXPENSE, true),
        cat("Коммунальные услуги", "utilities", EntryType.EXPENSE, true),
        cat("Транспорт", "transport", EntryType.EXPENSE, true),
        cat("Здоровье", "health", EntryType.EXPENSE, true),
        cat("Одежда", "clothes", EntryType.EXPENSE, false),
        cat("Образование", "education", EntryType.EXPENSE, false),
        cat("Развлечения", "fun", EntryType.EXPENSE, false),
        cat("Рестораны / кафе", "cafe", EntryType.EXPENSE, false),
        cat("Кредиты", "credit", EntryType.EXPENSE, true),
        cat("Связь и интернет", "internet", EntryType.EXPENSE, true),
        cat("Подписки", "subscriptions", EntryType.EXPENSE, false),
        cat("Семья", "family", EntryType.EXPENSE, true),
        cat("Покупки", "shopping", EntryType.EXPENSE, false),
        cat(OTHER, "other", EntryType.EXPENSE, false),
    )

    val income: List<Category> = listOf(
        cat("Зарплата", "salary", EntryType.INCOME, false),
        cat("Подработка", "side", EntryType.INCOME, false),
        cat("Премия", "bonus", EntryType.INCOME, false),
        cat("Аренда", "rent", EntryType.INCOME, false),
        cat("Фриланс", "freelance", EntryType.INCOME, false),
        cat(OTHER, "other", EntryType.INCOME, false),
    )

    val all: List<Category> get() = income + expense

    private fun cat(name: String, icon: String, type: EntryType, essential: Boolean) =
        Category(name = name, icon = icon, type = type, isDefault = true, isEssentialDefault = essential)
}
