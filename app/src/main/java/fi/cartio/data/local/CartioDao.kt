package fi.cartio.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import fi.cartio.core.model.SavedListIcon

@Dao
interface CartioDao {
    @Query("SELECT * FROM shopping_items ORDER BY sortOrder, id") fun observeItems(): Flow<List<ShoppingItemEntity>>
    @Insert suspend fun insertItem(item: ShoppingItemEntity): Long
    @Update suspend fun updateItem(item: ShoppingItemEntity)
    @Update suspend fun updateItems(items: List<ShoppingItemEntity>)
    @Query("SELECT * FROM shopping_items WHERE normalizedName = :normalizedName LIMIT 1") suspend fun findItem(normalizedName: String): ShoppingItemEntity?
    @Query("SELECT COALESCE(MAX(sortOrder), -1) + 1 FROM shopping_items") suspend fun nextSortOrder(): Int
    @Query("DELETE FROM shopping_items WHERE id = :id") suspend fun deleteItem(id: Long)
    @Query("DELETE FROM shopping_items") suspend fun clearItems()
    @Query("SELECT * FROM shopping_items") suspend fun getItems(): List<ShoppingItemEntity>
    @Query("UPDATE shopping_items SET checked = 0, updatedAt = :updatedAt WHERE checked = 1") suspend fun markAllItemsIncomplete(updatedAt: Long)
    @Query("DELETE FROM shopping_items WHERE checked = 1") suspend fun deleteCompletedItems()

    @Query(
        """SELECT lists.id, lists.name, lists.createdAt, lists.icon,
            COUNT(items.id) AS itemCount,
            COALESCE(SUM(CASE WHEN items.checked = 1 THEN 1 ELSE 0 END), 0) AS completedCount
            FROM saved_lists AS lists
            LEFT JOIN saved_list_items AS items ON items.listId = lists.id
            GROUP BY lists.id, lists.name, lists.createdAt, lists.icon
            ORDER BY lists.createdAt DESC""",
    )
    fun observeSavedLists(): Flow<List<SavedShoppingListSummary>>
    @Query("SELECT * FROM saved_lists WHERE id = :id") suspend fun getSavedList(id: Long): SavedShoppingListEntity?
    @Query("SELECT COUNT(*) FROM saved_list_items WHERE listId = :id") suspend fun savedItemCount(id: Long): Int
    @Insert suspend fun insertSavedList(list: SavedShoppingListEntity): Long
    @Insert suspend fun insertSavedItems(items: List<SavedShoppingListItemEntity>)
    @Query("SELECT * FROM saved_list_items WHERE listId = :id") suspend fun getSavedItems(id: Long): List<SavedShoppingListItemEntity>
    @Query("UPDATE saved_lists SET name = :name, icon = :icon WHERE id = :id") suspend fun updateSavedList(id: Long, name: String, icon: SavedListIcon)
    @Query("DELETE FROM saved_lists WHERE id = :id") suspend fun deleteSavedList(id: Long)
    @Query("DELETE FROM saved_list_items WHERE listId = :id") suspend fun deleteSavedItems(id: Long)

    @Query("SELECT * FROM active_list WHERE singletonId = 1") fun observeActiveList(): Flow<ActiveShoppingListEntity?>
    @Query("SELECT * FROM active_list WHERE singletonId = 1") suspend fun getActiveList(): ActiveShoppingListEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertActiveList(list: ActiveShoppingListEntity)
    @Query("DELETE FROM active_list") suspend fun clearActiveList()

    @Query("SELECT category FROM learned_categories WHERE normalizedName = :name") suspend fun learnedCategory(name: String): fi.cartio.core.model.ProductCategory?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun learn(entity: LearnedProductCategoryEntity)
    @Query("SELECT * FROM product_usage ORDER BY lastUsedAt DESC LIMIT :limit") suspend fun recent(limit: Int): List<ProductUsageEntity>
    @Query("SELECT * FROM product_usage ORDER BY useCount DESC, lastUsedAt DESC LIMIT :limit") suspend fun frequent(limit: Int): List<ProductUsageEntity>
    @Query("SELECT * FROM product_usage WHERE normalizedName = :name") suspend fun usage(name: String): ProductUsageEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertUsage(entity: ProductUsageEntity)

    @Transaction suspend fun saveCurrent(name: String): Long {
        val id = insertSavedList(SavedShoppingListEntity(name = name, createdAt = System.currentTimeMillis()))
        insertSavedItems(getItems().map { SavedShoppingListItemEntity(listId = id, name = it.name, normalizedName = it.normalizedName, quantity = it.quantity, unit = it.unit, category = it.category, checked = it.checked, sortOrder = it.sortOrder) })
        return id
    }

    @Transaction suspend fun restore(id: Long) {
        activateSavedList(id)
    }

    @Transaction suspend fun syncCurrentToActiveList() {
        val active = getActiveList() ?: return
        deleteSavedItems(active.savedListId)
        insertSavedItems(getItems().map { SavedShoppingListItemEntity(listId = active.savedListId, name = it.name, normalizedName = it.normalizedName, quantity = it.quantity, unit = it.unit, category = it.category, checked = it.checked, sortOrder = it.sortOrder) })
    }

    @Transaction suspend fun createAndActivateList(name: String, icon: SavedListIcon = SavedListIcon.CART): Long {
        syncCurrentToActiveList()
        val now = System.currentTimeMillis()
        val id = insertSavedList(SavedShoppingListEntity(name = name, createdAt = now, icon = icon))
        clearItems()
        upsertActiveList(ActiveShoppingListEntity(savedListId = id, name = name, createdAt = now, icon = icon))
        return id
    }

    @Transaction suspend fun activateSavedList(id: Long) {
        val list = getSavedList(id) ?: return
        syncCurrentToActiveList()
        clearItems()
        val now = System.currentTimeMillis()
        getSavedItems(id).forEach { insertItem(ShoppingItemEntity(name = it.name, normalizedName = it.normalizedName, quantity = it.quantity, unit = it.unit, category = it.category, checked = it.checked, createdAt = now, updatedAt = now, sortOrder = it.sortOrder)) }
        upsertActiveList(ActiveShoppingListEntity(savedListId = id, name = list.name, createdAt = list.createdAt, icon = list.icon))
    }

    @Transaction suspend fun restoreSavedList(list: SavedShoppingListEntity, items: List<SavedShoppingListItemEntity>) {
        insertSavedList(list)
        insertSavedItems(items)
    }

    @Transaction suspend fun deleteSavedSnapshot(id: Long): SavedListEntitySnapshot? {
        val list = getSavedList(id) ?: return null
        val items = getSavedItems(id)
        if (getActiveList()?.savedListId == id) {
            clearItems()
            clearActiveList()
        }
        deleteSavedList(id)
        return SavedListEntitySnapshot(list, items)
    }

    @Transaction suspend fun updateList(id: Long, name: String, icon: SavedListIcon) {
        updateSavedList(id, name, icon)
        getActiveList()?.takeIf { it.savedListId == id }?.let { upsertActiveList(it.copy(name = name, icon = icon)) }
    }

    @Transaction suspend fun reorderCurrent(items: List<ShoppingItemEntity>) {
        updateItems(items)
        syncCurrentToActiveList()
    }

    @Transaction suspend fun markAllIncomplete(): List<ShoppingItemEntity>? {
        val previous = getItems()
        if (previous.none { it.checked }) return null
        markAllItemsIncomplete(System.currentTimeMillis())
        syncCurrentToActiveList()
        return previous
    }

    @Transaction suspend fun removeCompleted(): List<ShoppingItemEntity>? {
        val previous = getItems()
        if (previous.none { it.checked }) return null
        deleteCompletedItems()
        syncCurrentToActiveList()
        return previous
    }

    @Transaction suspend fun replaceCurrent(items: List<ShoppingItemEntity>) {
        clearItems()
        items.forEach { insertItem(it) }
        syncCurrentToActiveList()
    }
}
