package fi.cartio

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.cartio.core.model.ProductCategory
import fi.cartio.data.local.CartioDatabase
import fi.cartio.data.local.LearnedProductCategoryEntity
import fi.cartio.data.local.ShoppingItemEntity
import fi.cartio.domain.suggestion.OfflineCategorySuggestionEngine
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
    @Before fun create() { db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), CartioDatabase::class.java).allowMainThreadQueries().build() }
    @After fun close() = db.close()
    @Test fun aliasesFallbackAndLearnedOverride() = runTest {
        val engine = OfflineCategorySuggestionEngine(db.dao())
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
    @Test fun englishAndFinnishQueriesProvideOneTapSuggestions() {
        val engine = OfflineCategorySuggestionEngine(db.dao())
        val repository = OfflineCartioRepository(db.dao(), engine)
        assertEquals("Eggs", repository.dictionarySuggestions("egg").single().name)
        assertEquals(ProductCategory.DAIRY, repository.dictionarySuggestions("egg").single().category)
        assertEquals("Appelsiini", repository.dictionarySuggestions("appels").single().name)
    }
}
