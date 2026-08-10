package fi.cartio.data.repository

import fi.cartio.core.model.ProductCategory
import fi.cartio.core.model.ProductSuggestion
import fi.cartio.core.model.AppLanguage
import fi.cartio.core.model.ActiveShoppingList
import fi.cartio.core.model.SavedShoppingList
import fi.cartio.core.model.SavedListSnapshot
import fi.cartio.core.model.SavedListIcon
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineCartioRepository @Inject constructor(private val dao: CartioDao, private val engine: CategorySuggestionEngine) : CartioRepository {
    override val items: Flow<List<ShoppingItem>> = dao.observeItems().map { list -> list.map { it.model() } }
    override val savedLists: Flow<List<SavedShoppingList>> = dao.observeSavedLists().map { lists ->
        lists.map { SavedShoppingList(it.id, it.name, it.itemCount, it.createdAt, it.completedCount, it.icon) }
    }
    override val activeList: Flow<ActiveShoppingList?> = combine(dao.observeActiveList(), items) { active, currentItems ->
        active?.let { ActiveShoppingList(it.savedListId, it.name, currentItems.size, currentItems.count(ShoppingItem::checked), it.icon) }
    }

    override suspend fun createList(name: String, icon: SavedListIcon) { dao.createAndActivateList(name.trim(), icon) }
    override suspend fun activateList(id: Long) { dao.activateSavedList(id) }

    override suspend fun add(name: String): ShoppingItem {
        val trimmed = name.trim().replaceFirstChar { it.uppercase() }
        val normalized = engine.normalize(trimmed)
        val category = engine.suggest(trimmed)
        val now = System.currentTimeMillis()
        dao.findItem(normalized)?.let { return it.model() }
        val entity = ShoppingItemEntity(name = trimmed, normalizedName = normalized, quantity = null, unit = null, category = category, checked = false, createdAt = now, updatedAt = now, sortOrder = dao.nextSortOrder())
        val id = dao.insertItem(entity)
        val previous = dao.usage(normalized)
        dao.upsertUsage(ProductUsageEntity(normalized, trimmed, category, (previous?.useCount ?: 0) + 1, now))
        dao.syncCurrentToActiveList()
        return entity.copy(id = id).model()
    }
    override suspend fun toggle(item: ShoppingItem) { dao.updateItem(item.copy(checked = !item.checked, updatedAt = System.currentTimeMillis()).entity()); dao.syncCurrentToActiveList() }
    override suspend fun update(item: ShoppingItem) {
        val normalized = engine.normalize(item.name)
        if (dao.findItem(normalized)?.id?.let { it != item.id } == true) return
        dao.updateItem(item.copy(normalizedName = normalized, updatedAt = System.currentTimeMillis()).entity())
        dao.learn(LearnedProductCategoryEntity(normalized, item.category))
        dao.syncCurrentToActiveList()
    }
    override suspend fun reorder(items: List<ShoppingItem>) = dao.reorderCurrent(items.mapIndexed { index, item -> item.copy(sortOrder = index).entity() })
    override suspend fun remove(id: Long) { dao.deleteItem(id); dao.syncCurrentToActiveList() }
    override suspend fun restoreItem(item: ShoppingItem) {
        if (dao.findItem(item.normalizedName) != null) return
        dao.insertItem(item.entity())
        dao.syncCurrentToActiveList()
    }
    override suspend fun save(name: String) { dao.saveCurrent(name.trim()) }
    override suspend fun restore(id: Long) = dao.activateSavedList(id)
    override suspend fun updateList(id: Long, name: String, icon: SavedListIcon) = dao.updateList(id, name.trim(), icon)
    override suspend fun deleteSaved(id: Long): SavedListSnapshot? {
        val deleted = dao.deleteSavedSnapshot(id) ?: return null
        val list = deleted.list
        val items = deleted.items
        return SavedListSnapshot(
            SavedShoppingList(list.id, list.name, items.size, list.createdAt, items.count { it.checked }, list.icon),
            items.map { ShoppingItem(it.id, it.name, it.normalizedName, it.quantity, it.unit, it.category, it.checked, sortOrder = it.sortOrder) },
        )
    }
    override suspend fun restoreSaved(snapshot: SavedListSnapshot) {
        dao.restoreSavedList(
            SavedShoppingListEntity(snapshot.list.id, snapshot.list.name, snapshot.list.createdAt, snapshot.list.icon),
            snapshot.items.map { SavedShoppingListItemEntity(id = it.id, listId = snapshot.list.id, name = it.name, normalizedName = it.normalizedName, quantity = it.quantity, unit = it.unit, category = it.category, checked = it.checked, sortOrder = it.sortOrder) },
        )
    }
    override suspend fun learn(name: String, category: ProductCategory) = dao.learn(LearnedProductCategoryEntity(engine.normalize(name), category))
    override suspend fun recent() = dao.recent(6).map { ProductSuggestion(it.displayName, it.category) }
    override suspend fun frequent() = dao.frequent(8).map { ProductSuggestion(it.displayName, it.category) }
    override fun dictionarySuggestions(query: String, language: AppLanguage): List<ProductSuggestion> {
        return engine.suggestions(query, language)
    }
}

private fun ShoppingItemEntity.model() = ShoppingItem(id, name, normalizedName, quantity, unit, category, checked, createdAt, updatedAt, sortOrder)
private fun ShoppingItem.entity() = ShoppingItemEntity(id, name, normalizedName, quantity, unit, category, checked, createdAt, updatedAt, sortOrder)
