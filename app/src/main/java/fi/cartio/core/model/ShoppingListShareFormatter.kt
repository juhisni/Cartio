package fi.cartio.core.model

fun formatShoppingListForSharing(
    listName: String,
    groupedItems: Map<ProductCategory, List<ShoppingItem>>,
    categoryNames: Map<ProductCategory, String>,
): String = buildString {
    appendLine(listName)
    groupedItems.forEach { (category, items) ->
        if (items.isEmpty()) return@forEach
        appendLine()
        appendLine(categoryNames[category] ?: category.name)
        items.forEach { item ->
            append(if (item.checked) "[x] " else "[ ] ")
            append(item.name)
            item.quantity?.let { append(" — ${formatQuantity(it, item.unit)}") }
            appendLine()
        }
    }
}.trimEnd()
