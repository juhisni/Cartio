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
}

private class FakeRepository : CartioRepository {
    private val mutableItems = MutableStateFlow<List<ShoppingItem>>(emptyList())
    override val items = mutableItems
    override val savedLists = MutableStateFlow<List<SavedShoppingList>>(emptyList())
    override suspend fun add(name: String): ShoppingItem = ShoppingItem(1, name.replaceFirstChar { it.uppercase() }, name.lowercase(), category = ProductCategory.DAIRY).also { mutableItems.value = listOf(it) }
    override suspend fun toggle(item: ShoppingItem) { mutableItems.value = listOf(item.copy(checked = !item.checked)) }
    override suspend fun remove(id: Long) { mutableItems.value = emptyList() }
    override suspend fun save(name: String) = Unit; override suspend fun restore(id: Long) = Unit; override suspend fun rename(id: Long, name: String) = Unit; override suspend fun deleteSaved(id: Long) = Unit
    override suspend fun learn(name: String, category: ProductCategory) = Unit
    override suspend fun recent() = emptyList<ProductSuggestion>(); override suspend fun frequent() = emptyList<ProductSuggestion>()
    override fun dictionarySuggestions(query: String) = emptyList<ProductSuggestion>()
}
