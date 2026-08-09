package fi.cartio.domain.repository

import fi.cartio.core.model.ProductCategory
import fi.cartio.core.model.ProductSuggestion
import fi.cartio.core.model.SavedShoppingList
import fi.cartio.core.model.ShoppingItem
import kotlinx.coroutines.flow.Flow

interface CartioRepository {
    val items: Flow<List<ShoppingItem>>
    val savedLists: Flow<List<SavedShoppingList>>
    suspend fun add(name: String): ShoppingItem
    suspend fun toggle(item: ShoppingItem)
    suspend fun remove(id: Long)
    suspend fun save(name: String)
    suspend fun restore(id: Long)
    suspend fun rename(id: Long, name: String)
    suspend fun deleteSaved(id: Long)
    suspend fun learn(name: String, category: ProductCategory)
    suspend fun recent(): List<ProductSuggestion>
    suspend fun frequent(): List<ProductSuggestion>
    fun dictionarySuggestions(query: String): List<ProductSuggestion>
}
