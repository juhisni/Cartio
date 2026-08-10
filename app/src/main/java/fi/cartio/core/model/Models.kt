package fi.cartio.core.model

enum class ProductCategory { FRUITS_VEGETABLES, DAIRY, BREAD_GRAINS, MEAT_FISH, FROZEN, PANTRY, DRINKS, HOUSEHOLD, OTHER }
enum class AppLanguage { FINNISH, ENGLISH }
enum class ThemePreference { SYSTEM, LIGHT, DARK }
enum class SavedListIcon(val symbol: String) { CART("🛒"), HOME("🏠"), PARTY("🎉"), GRILL("🔥"), GIFT("🎁"), TRAVEL("🧳") }

data class ShoppingItem(
    val id: Long = 0,
    val name: String,
    val normalizedName: String,
    val quantity: Double? = null,
    val unit: String? = null,
    val category: ProductCategory,
    val checked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0,
)

data class SavedShoppingList(val id: Long, val name: String, val itemCount: Int, val createdAt: Long, val completedCount: Int = 0, val icon: SavedListIcon = SavedListIcon.CART)
data class ActiveShoppingList(val savedListId: Long, val name: String, val itemCount: Int, val completedCount: Int, val icon: SavedListIcon = SavedListIcon.CART)
data class SavedListSnapshot(val list: SavedShoppingList, val items: List<ShoppingItem>)
data class ProductSuggestion(val name: String, val category: ProductCategory)
