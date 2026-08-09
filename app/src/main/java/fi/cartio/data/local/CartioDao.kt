package fi.cartio.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CartioDao {
    @Query("SELECT * FROM shopping_items ORDER BY checked, createdAt") fun observeItems(): Flow<List<ShoppingItemEntity>>
    @Insert suspend fun insertItem(item: ShoppingItemEntity): Long
    @Update suspend fun updateItem(item: ShoppingItemEntity)
    @Query("DELETE FROM shopping_items WHERE id = :id") suspend fun deleteItem(id: Long)
    @Query("DELETE FROM shopping_items") suspend fun clearItems()
    @Query("SELECT * FROM shopping_items") suspend fun getItems(): List<ShoppingItemEntity>

    @Query("SELECT * FROM saved_lists ORDER BY createdAt DESC") fun observeSavedLists(): Flow<List<SavedShoppingListEntity>>
    @Query("SELECT * FROM saved_lists WHERE id = :id") suspend fun getSavedList(id: Long): SavedShoppingListEntity?
    @Query("SELECT COUNT(*) FROM saved_list_items WHERE listId = :id") suspend fun savedItemCount(id: Long): Int
    @Insert suspend fun insertSavedList(list: SavedShoppingListEntity): Long
    @Insert suspend fun insertSavedItems(items: List<SavedShoppingListItemEntity>)
    @Query("SELECT * FROM saved_list_items WHERE listId = :id") suspend fun getSavedItems(id: Long): List<SavedShoppingListItemEntity>
    @Query("UPDATE saved_lists SET name = :name WHERE id = :id") suspend fun renameSavedList(id: Long, name: String)
    @Query("DELETE FROM saved_lists WHERE id = :id") suspend fun deleteSavedList(id: Long)

    @Query("SELECT category FROM learned_categories WHERE normalizedName = :name") suspend fun learnedCategory(name: String): fi.cartio.core.model.ProductCategory?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun learn(entity: LearnedProductCategoryEntity)
    @Query("SELECT * FROM product_usage ORDER BY lastUsedAt DESC LIMIT :limit") suspend fun recent(limit: Int): List<ProductUsageEntity>
    @Query("SELECT * FROM product_usage ORDER BY useCount DESC, lastUsedAt DESC LIMIT :limit") suspend fun frequent(limit: Int): List<ProductUsageEntity>
    @Query("SELECT * FROM product_usage WHERE normalizedName = :name") suspend fun usage(name: String): ProductUsageEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertUsage(entity: ProductUsageEntity)

    @Transaction suspend fun saveCurrent(name: String): Long {
        val id = insertSavedList(SavedShoppingListEntity(name = name, createdAt = System.currentTimeMillis()))
        insertSavedItems(getItems().map { SavedShoppingListItemEntity(listId = id, name = it.name, normalizedName = it.normalizedName, quantity = it.quantity, unit = it.unit, category = it.category, checked = it.checked) })
        return id
    }

    @Transaction suspend fun restore(id: Long) {
        clearItems()
        val now = System.currentTimeMillis()
        getSavedItems(id).forEach { insertItem(ShoppingItemEntity(name = it.name, normalizedName = it.normalizedName, quantity = it.quantity, unit = it.unit, category = it.category, checked = it.checked, createdAt = now, updatedAt = now)) }
    }

    @Transaction suspend fun restoreSavedList(list: SavedShoppingListEntity, items: List<SavedShoppingListItemEntity>) {
        insertSavedList(list)
        insertSavedItems(items)
    }

    @Transaction suspend fun deleteSavedSnapshot(id: Long): SavedListEntitySnapshot? {
        val list = getSavedList(id) ?: return null
        val items = getSavedItems(id)
        deleteSavedList(id)
        return SavedListEntitySnapshot(list, items)
    }
}
