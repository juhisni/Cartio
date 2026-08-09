package fi.cartio

import fi.cartio.core.model.ProductCategory
import fi.cartio.core.model.ProductSuggestion
import fi.cartio.core.model.SavedShoppingList
import fi.cartio.core.model.ShoppingItem
import fi.cartio.domain.repository.CartioRepository
import fi.cartio.feature.shoppinglist.ShoppingListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingListViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }
    @Test fun addingProductClearsQueryAndUpdatesGroup() = runTest(dispatcher) {
        val repository = FakeRepository(); val viewModel = ShoppingListViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }
        viewModel.setQuery("maito"); viewModel.add(); advanceUntilIdle()
        assertEquals("", viewModel.state.value.query)
        assertEquals("Maito", viewModel.state.value.groupedItems[ProductCategory.DAIRY]?.single()?.name)
    }

    @Test fun updatingProductMovesItToSelectedCategory() = runTest(dispatcher) {
        val repository = FakeRepository(); val viewModel = ShoppingListViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }
        viewModel.add("maito"); advanceUntilIdle()
        val item = viewModel.state.value.groupedItems[ProductCategory.DAIRY]!!.single()
        viewModel.update(item.copy(name = "Kauramaito", quantity = 2.0, unit = "l", category = ProductCategory.DRINKS)); advanceUntilIdle()
        val updated = viewModel.state.value.groupedItems[ProductCategory.DRINKS]!!.single()
        assertEquals("Kauramaito", updated.name)
        assertEquals(2.0, updated.quantity!!, 0.0)
        assertEquals("l", updated.unit)
    }

    @Test fun removedProductCanBeRestored() = runTest(dispatcher) {
        val repository = FakeRepository(); val viewModel = ShoppingListViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }
        viewModel.add("maito"); advanceUntilIdle()
        val item = viewModel.state.value.groupedItems[ProductCategory.DAIRY]!!.single()
        viewModel.remove(item); advanceUntilIdle(); assertEquals(0, viewModel.state.value.groupedItems.size)
        viewModel.undoRemove(item); advanceUntilIdle(); assertEquals("Maito", viewModel.state.value.groupedItems[ProductCategory.DAIRY]!!.single().name)
    }
}

private class FakeRepository : CartioRepository {
    private val mutableItems = MutableStateFlow<List<ShoppingItem>>(emptyList())
    override val items = mutableItems
    override val savedLists = MutableStateFlow<List<SavedShoppingList>>(emptyList())
    override suspend fun add(name: String): ShoppingItem = ShoppingItem(1, name.replaceFirstChar { it.uppercase() }, name.lowercase(), category = ProductCategory.DAIRY).also { mutableItems.value = listOf(it) }
    override suspend fun toggle(item: ShoppingItem) { mutableItems.value = listOf(item.copy(checked = !item.checked)) }
    override suspend fun update(item: ShoppingItem) { mutableItems.value = listOf(item) }
    override suspend fun remove(id: Long) { mutableItems.value = emptyList() }
    override suspend fun restoreItem(item: ShoppingItem) { mutableItems.value = listOf(item) }
    override suspend fun save(name: String) = Unit; override suspend fun restore(id: Long) = Unit; override suspend fun rename(id: Long, name: String) = Unit
    override suspend fun deleteSaved(id: Long): fi.cartio.core.model.SavedListSnapshot? = null
    override suspend fun restoreSaved(snapshot: fi.cartio.core.model.SavedListSnapshot) = Unit
    override suspend fun learn(name: String, category: ProductCategory) = Unit
    override suspend fun recent() = emptyList<ProductSuggestion>(); override suspend fun frequent() = emptyList<ProductSuggestion>()
    override fun dictionarySuggestions(query: String) = emptyList<ProductSuggestion>()
}
