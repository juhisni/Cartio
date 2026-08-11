package fi.cartio

import fi.cartio.core.model.ProductCategory
import fi.cartio.core.model.ShoppingItem
import fi.cartio.core.model.formatShoppingListForSharing
import org.junit.Assert.assertEquals
import org.junit.Test

class ShoppingListShareFormatterTest {
    @Test fun formatsCategoriesCompletionAndQuantitiesAsReadableText() {
        val items = linkedMapOf(
            ProductCategory.DAIRY to listOf(
                ShoppingItem(name = "Milk", normalizedName = "milk", quantity = 2.0, unit = "l", category = ProductCategory.DAIRY),
                ShoppingItem(name = "Cheese", normalizedName = "cheese", category = ProductCategory.DAIRY, checked = true),
            ),
            ProductCategory.BREAD_GRAINS to listOf(
                ShoppingItem(name = "Bread", normalizedName = "bread", category = ProductCategory.BREAD_GRAINS),
            ),
        )

        assertEquals(
            "Weekly groceries\n\nDairy\n[ ] Milk — 2 l\n[x] Cheese\n\nBread & grains\n[ ] Bread",
            formatShoppingListForSharing("Weekly groceries", items, mapOf(ProductCategory.DAIRY to "Dairy", ProductCategory.BREAD_GRAINS to "Bread & grains")),
        )
    }

    @Test fun formatsAnEmptyListAsItsName() {
        assertEquals("Empty list", formatShoppingListForSharing("Empty list", emptyMap(), emptyMap()))
    }
}
