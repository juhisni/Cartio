package fi.cartio.data.repository

import fi.cartio.core.model.ProductCategory
import fi.cartio.core.model.ProductSuggestion
import fi.cartio.core.model.SavedShoppingList
import fi.cartio.core.model.ShoppingItem
import fi.cartio.data.local.CartioDao
import fi.cartio.data.local.LearnedProductCategoryEntity
import fi.cartio.data.local.ProductUsageEntity
import fi.cartio.data.local.ShoppingItemEntity
import fi.cartio.domain.repository.CartioRepository
import fi.cartio.domain.suggestion.CategorySuggestionEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineCartioRepository @Inject constructor(private val dao: CartioDao, private val engine: CategorySuggestionEngine) : CartioRepository {
    override val items: Flow<List<ShoppingItem>> = dao.observeItems().map { list -> list.map { it.model() } }
    override val savedLists: Flow<List<SavedShoppingList>> = dao.observeSavedLists().map { lists -> lists.map { SavedShoppingList(it.id, it.name, dao.savedItemCount(it.id), it.createdAt) } }

    override suspend fun add(name: String): ShoppingItem {
        val trimmed = name.trim().replaceFirstChar { it.uppercase() }
        val normalized = engine.normalize(trimmed)
        val category = engine.suggest(trimmed)
        val now = System.currentTimeMillis()
        val entity = ShoppingItemEntity(name = trimmed, normalizedName = normalized, quantity = null, unit = null, category = category, checked = false, createdAt = now, updatedAt = now)
        val id = dao.insertItem(entity)
        val previous = dao.usage(normalized)
        dao.upsertUsage(ProductUsageEntity(normalized, trimmed, category, (previous?.useCount ?: 0) + 1, now))
        return entity.copy(id = id).model()
    }
    override suspend fun toggle(item: ShoppingItem) = dao.updateItem(item.copy(checked = !item.checked, updatedAt = System.currentTimeMillis()).entity())
    override suspend fun remove(id: Long) = dao.deleteItem(id)
    override suspend fun save(name: String) { dao.saveCurrent(name.trim()) }
    override suspend fun restore(id: Long) = dao.restore(id)
    override suspend fun rename(id: Long, name: String) = dao.renameSavedList(id, name.trim())
    override suspend fun deleteSaved(id: Long) = dao.deleteSavedList(id)
    override suspend fun learn(name: String, category: ProductCategory) = dao.learn(LearnedProductCategoryEntity(engine.normalize(name), category))
    override suspend fun recent() = dao.recent(6).map { ProductSuggestion(it.displayName, it.category) }
    override suspend fun frequent() = dao.frequent(8).map { ProductSuggestion(it.displayName, it.category) }
    override fun dictionarySuggestions(query: String): List<ProductSuggestion> {
        val q = engine.normalize(query)
        val values = listOf("Banaani" to ProductCategory.FRUITS_VEGETABLES, "Maito" to ProductCategory.DAIRY, "Leipä" to ProductCategory.BREAD_GRAINS, "Kananmunat" to ProductCategory.DAIRY, "Juusto" to ProductCategory.DAIRY, "Kurkku" to ProductCategory.FRUITS_VEGETABLES, "Pasta" to ProductCategory.PANTRY, "Lohi" to ProductCategory.MEAT_FISH)
        return values.filter { q.isBlank() || engine.normalize(it.first).contains(q) }.map { ProductSuggestion(it.first, it.second) }
    }
}

private fun ShoppingItemEntity.model() = ShoppingItem(id, name, normalizedName, quantity, unit, category, checked, createdAt, updatedAt)
private fun ShoppingItem.entity() = ShoppingItemEntity(id, name, normalizedName, quantity, unit, category, checked, createdAt, updatedAt)
