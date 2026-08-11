package fi.cartio

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.cartio.core.model.AppLanguage
import fi.cartio.core.model.ProductCategory
import fi.cartio.core.model.ProductSuggestion
import fi.cartio.core.model.SavedListIcon
import fi.cartio.data.local.CartioDatabase
import fi.cartio.data.local.LearnedProductCategoryEntity
import fi.cartio.data.local.ShoppingItemEntity
import fi.cartio.domain.suggestion.OfflineCategorySuggestionEngine
import fi.cartio.domain.suggestion.BundledProductCatalog
import fi.cartio.data.repository.OfflineCartioRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CartioDatabaseTest {
    private lateinit var db: CartioDatabase
    private lateinit var context: Context
    @Before fun create() { context = ApplicationProvider.getApplicationContext(); db = Room.inMemoryDatabaseBuilder(context, CartioDatabase::class.java).allowMainThreadQueries().build() }
    @After fun close() = db.close()
    @Test fun aliasesFallbackAndLearnedOverride() = runTest {
        val engine = OfflineCategorySuggestionEngine(db.dao(), BundledProductCatalog(context))
        assertEquals(ProductCategory.DAIRY, engine.suggest("maito")); assertEquals(ProductCategory.DAIRY, engine.suggest("milk"))
        assertEquals(ProductCategory.FRUITS_VEGETABLES, engine.suggest("banana")); assertEquals(ProductCategory.OTHER, engine.suggest("mystery item"))
        db.dao().learn(LearnedProductCategoryEntity("mystery item", ProductCategory.PANTRY)); assertEquals(ProductCategory.PANTRY, engine.suggest("mystery item"))
    }
    @Test fun addToggleRemoveSaveAndRestore() = runTest {
        val dao = db.dao(); val now = System.currentTimeMillis(); val item = ShoppingItemEntity(name = "Maito", normalizedName = "maito", quantity = null, unit = null, category = ProductCategory.DAIRY, checked = false, createdAt = now, updatedAt = now)
        val id = dao.insertItem(item); dao.updateItem(item.copy(id = id, checked = true)); assertEquals(true, dao.observeItems().first().single().checked)
        val savedId = dao.saveCurrent("Viikon ostokset"); dao.deleteItem(id); assertEquals(0, dao.observeItems().first().size)
        val deleted = dao.deleteSavedSnapshot(savedId)!!; assertEquals(0, dao.observeSavedLists().first().size)
        dao.restoreSavedList(deleted.list, deleted.items); assertEquals("Viikon ostokset", dao.observeSavedLists().first().single().name)
        dao.restore(savedId); assertEquals("Maito", dao.observeItems().first().single().name); dao.deleteItem(dao.observeItems().first().single().id); assertEquals(0, dao.observeItems().first().size)
    }
    @Test fun namedListsPersistAndSwitchWithoutMixingItems() = runTest {
        val dao = db.dao()
        val weeklyId = dao.createAndActivateList("Weekly groceries")
        val now = System.currentTimeMillis()
        dao.insertItem(
            ShoppingItemEntity(
                name = "Milk",
                normalizedName = "milk",
                quantity = null,
                unit = null,
                category = ProductCategory.DAIRY,
                checked = false,
                createdAt = now,
                updatedAt = now,
            ),
        )
        dao.syncCurrentToActiveList()
        var weekly = dao.observeSavedLists().first().single()
        assertEquals(1, weekly.itemCount)
        assertEquals(0, weekly.completedCount)

        val milk = dao.observeItems().first().single()
        dao.updateItem(milk.copy(checked = true))
        dao.syncCurrentToActiveList()
        weekly = dao.observeSavedLists().first().single()
        assertEquals(1, weekly.completedCount)

        val partyId = dao.createAndActivateList("Party")
        assertEquals(0, dao.observeItems().first().size)
        assertEquals(partyId, dao.observeActiveList().first()?.savedListId)

        dao.activateSavedList(weeklyId)
        assertEquals("Milk", dao.observeItems().first().single().name)
        assertEquals("Weekly groceries", dao.observeActiveList().first()?.name)

        dao.updateList(weeklyId, "Every week", SavedListIcon.HOME)
        assertEquals("Every week", dao.observeActiveList().first()?.name)
        assertEquals(SavedListIcon.HOME, dao.observeActiveList().first()?.icon)
        assertEquals(2, dao.observeSavedLists().first().size)
    }
    @Test fun englishAndFinnishQueriesProvideOneTapSuggestions() {
        val catalog = BundledProductCatalog(context)
        val engine = OfflineCategorySuggestionEngine(db.dao(), catalog)
        val repository = OfflineCartioRepository(db.dao(), engine)
        assertEquals(535, catalog.products.size)
        val eggs = repository.dictionarySuggestions("egg", AppLanguage.ENGLISH)
        assertEquals(ProductCategory.DAIRY, eggs.first { it.name == "Egg" }.category)
        assertEquals(false, eggs.any { it.name == "Eggs" })
        val oats = repository.dictionarySuggestions("oat", AppLanguage.ENGLISH)
        assertEquals(false, oats.any { it.name == "Oats" })
        assertEquals("Appelsiini", repository.dictionarySuggestions("appels", AppLanguage.FINNISH).first().name)
        assertEquals(emptyList<ProductSuggestion>(), repository.dictionarySuggestions("egg", AppLanguage.FINNISH))
        assertEquals(emptyList<ProductSuggestion>(), repository.dictionarySuggestions("jauhe", AppLanguage.ENGLISH))
    }
    @Test fun repositoryDoesNotAddTheSameProductTwice() = runTest {
        val engine = OfflineCategorySuggestionEngine(db.dao(), BundledProductCatalog(context))
        val repository = OfflineCartioRepository(db.dao(), engine)
        db.dao().createAndActivateList("Duplicates")

        repository.add("Milk")
        repository.add("milk")

        assertEquals(1, repository.items.first().size)
    }

    @Test fun bulkCompletionActionsSyncAndRestoreTheActiveList() = runTest {
        val dao = db.dao()
        val listId = dao.createAndActivateList("Weekly")
        val now = System.currentTimeMillis()
        dao.insertItem(ShoppingItemEntity(name = "Milk", normalizedName = "milk", quantity = null, unit = null, category = ProductCategory.DAIRY, checked = true, createdAt = now, updatedAt = now, sortOrder = 0))
        dao.insertItem(ShoppingItemEntity(name = "Bread", normalizedName = "bread", quantity = null, unit = null, category = ProductCategory.BREAD_GRAINS, checked = false, createdAt = now, updatedAt = now, sortOrder = 1))
        dao.syncCurrentToActiveList()

        val beforeReset = dao.markAllIncomplete()!!
        assertEquals(0, dao.observeItems().first().count { it.checked })
        assertEquals(0, dao.observeSavedLists().first().single { it.id == listId }.completedCount)

        dao.replaceCurrent(beforeReset)
        assertEquals(1, dao.observeItems().first().count { it.checked })

        val beforeRemoval = dao.removeCompleted()!!
        assertEquals(listOf("Bread"), dao.observeItems().first().map { it.name })
        dao.replaceCurrent(beforeRemoval)
        assertEquals(listOf("Milk", "Bread"), dao.observeItems().first().map { it.name })
    }

    @Test fun duplicateListCopiesMetadataAndEveryProductWithoutActivatingIt() = runTest {
        val dao = db.dao()
        val sourceId = dao.createAndActivateList("Weekly", SavedListIcon.HOME)
        val now = System.currentTimeMillis()
        dao.insertItem(ShoppingItemEntity(name = "Milk", normalizedName = "milk", quantity = 2.0, unit = "l", category = ProductCategory.DAIRY, checked = true, createdAt = now, updatedAt = now))
        dao.syncCurrentToActiveList()

        val duplicateId = dao.duplicateSavedList(sourceId, "Weekly – copy")!!
        val duplicate = dao.getSavedList(duplicateId)!!
        val duplicateItem = dao.getSavedItems(duplicateId).single()

        assertEquals("Weekly – copy", duplicate.name)
        assertEquals(SavedListIcon.HOME, duplicate.icon)
        assertEquals("Milk", duplicateItem.name)
        assertEquals(2.0, duplicateItem.quantity)
        assertEquals("l", duplicateItem.unit)
        assertEquals(true, duplicateItem.checked)
        assertEquals(sourceId, dao.observeActiveList().first()?.savedListId)
    }
}
