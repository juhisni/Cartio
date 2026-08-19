package fi.cartio.domain.repository

import fi.cartio.core.model.ProductCategory
import fi.cartio.core.model.ProductSuggestion
import fi.cartio.core.model.AppLanguage
import fi.cartio.core.model.ActiveShoppingList
import fi.cartio.core.model.SavedShoppingList
import fi.cartio.core.model.SavedListSnapshot
import fi.cartio.core.model.ShoppingItem
import fi.cartio.core.model.SavedListIcon
import kotlinx.coroutines.flow.Flow

interface CartioRepository {
    val items: Flow<List<ShoppingItem>>
    val savedLists: Flow<List<SavedShoppingList>>
    val activeList: Flow<ActiveShoppingList?>
    suspend fun createList(name: String, icon: SavedListIcon): Long?
    suspend fun activateList(id: Long): Boolean
    suspend fun add(name: String, categoryOverride: ProductCategory? = null): ShoppingItem
    suspend fun toggle(item: ShoppingItem)
    suspend fun update(item: ShoppingItem): Boolean
    suspend fun reorder(items: List<ShoppingItem>)
    suspend fun markAllIncomplete(): List<ShoppingItem>?
    suspend fun removeCompleted(): List<ShoppingItem>?
    suspend fun restoreCurrent(items: List<ShoppingItem>)
    suspend fun remove(id: Long)
    suspend fun restoreItem(item: ShoppingItem)
    suspend fun save(name: String)
    suspend fun restore(id: Long): Boolean
    suspend fun updateList(id: Long, name: String, icon: SavedListIcon): Boolean
    suspend fun duplicateList(id: Long, name: String): Long?
    suspend fun deleteSaved(id: Long): SavedListSnapshot?
    suspend fun restoreSaved(snapshot: SavedListSnapshot)
    suspend fun learn(name: String, category: ProductCategory)
    suspend fun recent(): List<ProductSuggestion>
    suspend fun frequent(): List<ProductSuggestion>
    fun dictionarySuggestions(query: String, language: AppLanguage): List<ProductSuggestion>
}
