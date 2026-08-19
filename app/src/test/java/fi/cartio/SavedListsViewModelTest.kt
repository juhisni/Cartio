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
import kotlinx.coroutines.async
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
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

    @Test fun activeListIsAlwaysShownFirst() = runTest(dispatcher) {
        val repository = SavedListsFakeRepository().apply {
            activeList.value = ActiveShoppingList(2, "Juhlat", 8, 3)
        }
        val viewModel = SavedListsViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

        advanceUntilIdle()

        assertEquals(listOf(2L, 1L), viewModel.state.value.lists.map { it.id })
    }

    @Test fun duplicationPublishesCreatedListForFeedbackAndOpening() = runTest(dispatcher) {
        val viewModel = SavedListsViewModel(SavedListsFakeRepository())
        val event = async { viewModel.duplications.first() }

        viewModel.duplicate(1, "Viikon ostokset – kopio")
        advanceUntilIdle()

        assertEquals(3L, event.await().id)
        assertEquals("Viikon ostokset – kopio", event.await().name)
    }

    @Test fun successfulRestorePublishesNavigationOnlyAfterRepositoryCompletes() = runTest(dispatcher) {
        val viewModel = SavedListsViewModel(SavedListsFakeRepository())
        val navigation = async { viewModel.navigation.first() }

        viewModel.restore(2)
        advanceUntilIdle()

        navigation.await()
    }

    @Test fun duplicateListNamePublishesConflictInsteadOfNavigation() = runTest(dispatcher) {
        val viewModel = SavedListsViewModel(SavedListsFakeRepository(createResult = null))
        val conflict = async { viewModel.nameConflicts.first() }

        viewModel.create("Viikon ostokset", SavedListIcon.CART)
        advanceUntilIdle()

        conflict.await()
    }

    @Test fun createOperationExposesBusyStateAndPreventsRepeatedSubmission() = runTest(dispatcher) {
        val gate = CompletableDeferred<Unit>()
        val repository = SavedListsFakeRepository(createGate = gate)
        val viewModel = SavedListsViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

        viewModel.create("New list", SavedListIcon.CART)
        viewModel.create("New list", SavedListIcon.CART)
        runCurrent()
        assertTrue(viewModel.state.value.isBusy)
        assertEquals(1, repository.createCalls)

        gate.complete(Unit)
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isBusy)
    }
}

private class SavedListsFakeRepository(private val createResult: Long? = 3L, private val createGate: CompletableDeferred<Unit>? = null) : CartioRepository {
    var createCalls = 0
    override val items = MutableStateFlow<List<ShoppingItem>>(emptyList())
    override val savedLists = MutableStateFlow(listOf(
        SavedShoppingList(1, "Viikon ostokset", 12, 1),
        SavedShoppingList(2, "Juhlat", 8, 2),
    ))
    override val activeList = MutableStateFlow<ActiveShoppingList?>(null)
    override suspend fun createList(name: String, icon: SavedListIcon): Long? {
        createCalls++
        createGate?.await()
        return createResult
    }
    override suspend fun activateList(id: Long) = true
    override suspend fun add(name: String, categoryOverride: ProductCategory?) = error("Not used")
    override suspend fun toggle(item: ShoppingItem) = Unit
    override suspend fun update(item: ShoppingItem) = true
    override suspend fun reorder(items: List<ShoppingItem>) = Unit
    override suspend fun markAllIncomplete(): List<ShoppingItem>? = null
    override suspend fun removeCompleted(): List<ShoppingItem>? = null
    override suspend fun restoreCurrent(items: List<ShoppingItem>) = Unit
    override suspend fun remove(id: Long) = Unit
    override suspend fun restoreItem(item: ShoppingItem) = Unit
    override suspend fun save(name: String) = Unit
    override suspend fun restore(id: Long) = true
    override suspend fun updateList(id: Long, name: String, icon: SavedListIcon) = true
    override suspend fun duplicateList(id: Long, name: String): Long? = 3L
    override suspend fun deleteSaved(id: Long): SavedListSnapshot? = null
    override suspend fun restoreSaved(snapshot: SavedListSnapshot) = Unit
    override suspend fun learn(name: String, category: ProductCategory) = Unit
    override suspend fun recent() = emptyList<ProductSuggestion>()
    override suspend fun frequent() = emptyList<ProductSuggestion>()
    override fun dictionarySuggestions(query: String, language: AppLanguage) = emptyList<ProductSuggestion>()
}
