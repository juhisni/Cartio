package fi.cartio.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import fi.cartio.core.model.ProductCategory

@Entity(tableName = "shopping_items", indices = [Index("normalizedName")])
data class ShoppingItemEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String, val normalizedName: String, val quantity: Double?, val unit: String?, val category: ProductCategory, val checked: Boolean, val createdAt: Long, val updatedAt: Long)

@Entity(tableName = "saved_lists")
data class SavedShoppingListEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String, val createdAt: Long)

data class SavedShoppingListSummary(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val itemCount: Int,
    val completedCount: Int,
)

@Entity(tableName = "active_list")
data class ActiveShoppingListEntity(
    @PrimaryKey val singletonId: Int = 1,
    val savedListId: Long,
    val name: String,
    val createdAt: Long,
)

@Entity(tableName = "saved_list_items", foreignKeys = [ForeignKey(entity = SavedShoppingListEntity::class, parentColumns = ["id"], childColumns = ["listId"], onDelete = ForeignKey.CASCADE)], indices = [Index("listId")])
data class SavedShoppingListItemEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val listId: Long, val name: String, val normalizedName: String, val quantity: Double?, val unit: String?, val category: ProductCategory, val checked: Boolean)

@Entity(tableName = "learned_categories")
data class LearnedProductCategoryEntity(@PrimaryKey val normalizedName: String, val category: ProductCategory)

@Entity(tableName = "product_usage")
data class ProductUsageEntity(@PrimaryKey val normalizedName: String, val displayName: String, val category: ProductCategory, val useCount: Int, val lastUsedAt: Long)

data class SavedListEntitySnapshot(val list: SavedShoppingListEntity, val items: List<SavedShoppingListItemEntity>)
