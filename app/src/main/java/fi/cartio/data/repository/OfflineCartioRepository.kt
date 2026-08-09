package fi.cartio.data.repository

import fi.cartio.core.model.ProductCategory
import fi.cartio.core.model.ProductSuggestion
import fi.cartio.core.model.SavedShoppingList
import fi.cartio.core.model.SavedListSnapshot
import fi.cartio.core.model.ShoppingItem
import fi.cartio.data.local.CartioDao
import fi.cartio.data.local.LearnedProductCategoryEntity
import fi.cartio.data.local.ProductUsageEntity
import fi.cartio.data.local.ShoppingItemEntity
import fi.cartio.data.local.SavedShoppingListEntity
import fi.cartio.data.local.SavedShoppingListItemEntity
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
    override suspend fun update(item: ShoppingItem) {
        val normalized = engine.normalize(item.name)
        dao.updateItem(item.copy(normalizedName = normalized, updatedAt = System.currentTimeMillis()).entity())
        dao.learn(LearnedProductCategoryEntity(normalized, item.category))
    }
    override suspend fun remove(id: Long) = dao.deleteItem(id)
    override suspend fun restoreItem(item: ShoppingItem) { dao.insertItem(item.entity()) }
    override suspend fun save(name: String) { dao.saveCurrent(name.trim()) }
    override suspend fun restore(id: Long) = dao.restore(id)
    override suspend fun rename(id: Long, name: String) = dao.renameSavedList(id, name.trim())
    override suspend fun deleteSaved(id: Long): SavedListSnapshot? {
        val deleted = dao.deleteSavedSnapshot(id) ?: return null
        val list = deleted.list
        val items = deleted.items
        return SavedListSnapshot(
            SavedShoppingList(list.id, list.name, items.size, list.createdAt),
            items.map { ShoppingItem(it.id, it.name, it.normalizedName, it.quantity, it.unit, it.category, it.checked) },
        )
    }
    override suspend fun restoreSaved(snapshot: SavedListSnapshot) {
        dao.restoreSavedList(
            SavedShoppingListEntity(snapshot.list.id, snapshot.list.name, snapshot.list.createdAt),
            snapshot.items.map { SavedShoppingListItemEntity(id = it.id, listId = snapshot.list.id, name = it.name, normalizedName = it.normalizedName, quantity = it.quantity, unit = it.unit, category = it.category, checked = it.checked) },
        )
    }
    override suspend fun learn(name: String, category: ProductCategory) = dao.learn(LearnedProductCategoryEntity(engine.normalize(name), category))
    override suspend fun recent() = dao.recent(6).map { ProductSuggestion(it.displayName, it.category) }
    override suspend fun frequent() = dao.frequent(8).map { ProductSuggestion(it.displayName, it.category) }
    override fun dictionarySuggestions(query: String): List<ProductSuggestion> {
        val q = engine.normalize(query)
        val matches = if (q.isBlank()) bundledSuggestions.filter { it.defaultSuggestion } else bundledSuggestions.filter { engine.normalize(it.name).contains(q) }
        return matches.take(8).map { ProductSuggestion(it.name, it.category) }
    }
}

private data class BundledSuggestion(val name: String, val category: ProductCategory, val defaultSuggestion: Boolean = false)

private val bundledSuggestions = listOf(
    BundledSuggestion("Banaani", ProductCategory.FRUITS_VEGETABLES, true), BundledSuggestion("Banana", ProductCategory.FRUITS_VEGETABLES),
    BundledSuggestion("Maito", ProductCategory.DAIRY, true), BundledSuggestion("Milk", ProductCategory.DAIRY),
    BundledSuggestion("Leipä", ProductCategory.BREAD_GRAINS, true), BundledSuggestion("Bread", ProductCategory.BREAD_GRAINS),
    BundledSuggestion("Kananmunat", ProductCategory.DAIRY, true), BundledSuggestion("Eggs", ProductCategory.DAIRY),
    BundledSuggestion("Juusto", ProductCategory.DAIRY, true), BundledSuggestion("Cheese", ProductCategory.DAIRY),
    BundledSuggestion("Kurkku", ProductCategory.FRUITS_VEGETABLES, true), BundledSuggestion("Cucumber", ProductCategory.FRUITS_VEGETABLES),
    BundledSuggestion("Pasta", ProductCategory.PANTRY, true), BundledSuggestion("Lohi", ProductCategory.MEAT_FISH, true), BundledSuggestion("Salmon", ProductCategory.MEAT_FISH),
    BundledSuggestion("Omena", ProductCategory.FRUITS_VEGETABLES), BundledSuggestion("Apple", ProductCategory.FRUITS_VEGETABLES),
    BundledSuggestion("Appelsiini", ProductCategory.FRUITS_VEGETABLES), BundledSuggestion("Orange", ProductCategory.FRUITS_VEGETABLES),
    BundledSuggestion("Peruna", ProductCategory.FRUITS_VEGETABLES), BundledSuggestion("Potato", ProductCategory.FRUITS_VEGETABLES),
    BundledSuggestion("Kana", ProductCategory.MEAT_FISH), BundledSuggestion("Chicken", ProductCategory.MEAT_FISH),
    BundledSuggestion("Riisi", ProductCategory.BREAD_GRAINS), BundledSuggestion("Rice", ProductCategory.BREAD_GRAINS),
    BundledSuggestion("Kahvi", ProductCategory.PANTRY), BundledSuggestion("Coffee", ProductCategory.PANTRY),
    BundledSuggestion("Tee", ProductCategory.PANTRY), BundledSuggestion("Tea", ProductCategory.PANTRY),
    BundledSuggestion("Vesi", ProductCategory.DRINKS), BundledSuggestion("Water", ProductCategory.DRINKS),
    BundledSuggestion("Talouspaperi", ProductCategory.HOUSEHOLD), BundledSuggestion("Paper towel", ProductCategory.HOUSEHOLD),
)

private fun ShoppingItemEntity.model() = ShoppingItem(id, name, normalizedName, quantity, unit, category, checked, createdAt, updatedAt)
private fun ShoppingItem.entity() = ShoppingItemEntity(id, name, normalizedName, quantity, unit, category, checked, createdAt, updatedAt)
