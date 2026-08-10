package fi.cartio

import fi.cartio.core.model.AppLanguage
import fi.cartio.core.model.ActiveShoppingList
import fi.cartio.core.model.ProductCategory
import fi.cartio.core.model.ProductSuggestion
import fi.cartio.core.model.SavedListSnapshot
import fi.cartio.core.model.SavedShoppingList
import fi.cartio.core.model.ShoppingItem
import fi.cartio.core.model.SavedListIcon
import fi.cartio.domain.repository.CartioRepository
import fi.cartio.feature.savedlists.SavedListsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SavedListsViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setup() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun searchFiltersSavedListsIgnoringCase() = runTest(dispatcher) {
        val repository = SavedListsFakeRepository()
        val viewModel = SavedListsViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

        viewModel.setQuery("VIIKON")
        advanceUntilIdle()

        assertEquals(listOf("Viikon ostokset"), viewModel.state.value.lists.map { it.name })
        assertTrue(viewModel.state.value.hasSavedLists)
    }

    @Test fun unmatchedSearchKeepsSavedListPresenceInState() = runTest(dispatcher) {
        val repository = SavedListsFakeRepository()
        val viewModel = SavedListsViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

        viewModel.setQuery("matkalle")
        advanceUntilIdle()

        assertTrue(viewModel.state.value.lists.isEmpty())
        assertTrue(viewModel.state.value.hasSavedLists)
    }
}

private class SavedListsFakeRepository : CartioRepository {
    override val items = MutableStateFlow<List<ShoppingItem>>(emptyList())
    override val savedLists = MutableStateFlow(listOf(
        SavedShoppingList(1, "Viikon ostokset", 12, 1),
        SavedShoppingList(2, "Juhlat", 8, 2),
    ))
    override val activeList = MutableStateFlow<ActiveShoppingList?>(null)
    override suspend fun createList(name: String, icon: SavedListIcon) = Unit
    override suspend fun activateList(id: Long) = Unit
    override suspend fun add(name: String) = error("Not used")
    override suspend fun toggle(item: ShoppingItem) = Unit
    override suspend fun update(item: ShoppingItem) = Unit
    override suspend fun reorder(items: List<ShoppingItem>) = Unit
    override suspend fun remove(id: Long) = Unit
    override suspend fun restoreItem(item: ShoppingItem) = Unit
    override suspend fun save(name: String) = Unit
    override suspend fun restore(id: Long) = Unit
    override suspend fun updateList(id: Long, name: String, icon: SavedListIcon) = Unit
    override suspend fun deleteSaved(id: Long): SavedListSnapshot? = null
    override suspend fun restoreSaved(snapshot: SavedListSnapshot) = Unit
    override suspend fun learn(name: String, category: ProductCategory) = Unit
    override suspend fun recent() = emptyList<ProductSuggestion>()
    override suspend fun frequent() = emptyList<ProductSuggestion>()
    override fun dictionarySuggestions(query: String, language: AppLanguage) = emptyList<ProductSuggestion>()
}
