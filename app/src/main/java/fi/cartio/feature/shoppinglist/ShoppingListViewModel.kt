package fi.cartio.feature.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fi.cartio.core.model.ProductCategory
import fi.cartio.core.model.ProductSuggestion
import fi.cartio.core.model.ShoppingItem
import fi.cartio.core.model.AppLanguage
import fi.cartio.core.model.ActiveShoppingList
import fi.cartio.core.model.SavedShoppingList
import fi.cartio.core.model.SavedListSnapshot
import fi.cartio.core.model.SavedListIcon
import fi.cartio.domain.repository.CartioRepository
import fi.cartio.domain.suggestion.normalizeProductInput
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch

data class ShoppingListUiState(
    val groupedItems: Map<ProductCategory, List<ShoppingItem>> = emptyMap(),
    val query: String = "",
    val suggestions: List<ProductSuggestion> = emptyList(),
    val recent: List<ProductSuggestion> = emptyList(),
    val frequent: List<ProductSuggestion> = emptyList(),
    val activeList: ActiveShoppingList? = null,
    val savedLists: List<SavedShoppingList> = emptyList(),
    val canAddQuery: Boolean = false,
)

enum class BulkListAction { MARKED_INCOMPLETE, REMOVED_COMPLETED }
data class BulkListChange(val action: BulkListAction, val previousItems: List<ShoppingItem>)

internal fun exactCatalogMatch(
    query: String,
    suggestions: List<ProductSuggestion>,
): ProductSuggestion? {
    val normalizedQuery = normalizeProductInput(query)
    return suggestions.firstOrNull { normalizeProductInput(it.name) == normalizedQuery }
}

@HiltViewModel
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class ShoppingListViewModel @Inject constructor(private val repository: CartioRepository) : ViewModel() {
    private val query = MutableStateFlow("")
    private val language = MutableStateFlow(AppLanguage.ENGLISH)
    private val history = MutableStateFlow(Pair(emptyList<ProductSuggestion>(), emptyList<ProductSuggestion>()))
    private val suggestions = combine(query.debounce(100).distinctUntilChanged(), language) { text, selectedLanguage ->
        text to selectedLanguage
    }.mapLatest { (text, selectedLanguage) ->
        repository.dictionarySuggestions(text, selectedLanguage)
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    private val listContext = combine(repository.items, repository.activeList, repository.savedLists) { items, active, saved -> Triple(items, active, saved) }
    val state: StateFlow<ShoppingListUiState> = combine(listContext, query, history, suggestions) { context, text, usage, matches ->
        val existing = context.first.map { it.normalizedName }.toSet()
        fun available(values: List<ProductSuggestion>) = values.filterNot { normalizeProductInput(it.name) in existing }
        ShoppingListUiState(
            groupedItems = context.first.groupBy { it.category },
            query = text,
            suggestions = available(matches),
            recent = available(usage.first),
            frequent = available(usage.second),
            activeList = context.second,
            savedLists = context.third,
            canAddQuery = text.isNotBlank() && normalizeProductInput(text) !in existing,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShoppingListUiState())
    private val feedbackChannel = Channel<String>(Channel.BUFFERED)
    val feedback = feedbackChannel.receiveAsFlow()
    private val removalChannel = Channel<ShoppingItem>(Channel.BUFFERED)
    val removals = removalChannel.receiveAsFlow()
    private val bulkChangeChannel = Channel<BulkListChange>(Channel.BUFFERED)
    val bulkChanges = bulkChangeChannel.receiveAsFlow()
    private val deletedListChannel = Channel<SavedListSnapshot>(Channel.BUFFERED)
    val deletedLists = deletedListChannel.receiveAsFlow()

    init { refreshHistory() }
    fun setQuery(value: String) { query.value = value }
    fun setLanguage(value: AppLanguage) { language.value = value }
    fun createList(name: String, icon: SavedListIcon = SavedListIcon.CART) { if (name.isNotBlank()) viewModelScope.launch { repository.createList(name, icon) } }
    fun activateList(id: Long) { viewModelScope.launch { repository.activateList(id) } }
    fun addCatalogMatch() {
        val match = exactCatalogMatch(query.value, state.value.suggestions) ?: return
        add(match.name)
    }
    fun add(name: String = query.value) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val item = repository.add(name)
            query.value = ""
            feedbackChannel.send(item.name)
            refreshHistory()
        }
    }
    fun toggle(item: ShoppingItem) { viewModelScope.launch { repository.toggle(item) } }
    fun update(item: ShoppingItem) { viewModelScope.launch { repository.update(item) } }
    fun moveItem(item: ShoppingItem, direction: Int) {
        val ordered = state.value.groupedItems.values.flatten().toMutableList()
        val from = ordered.indexOfFirst { it.id == item.id }
        if (from < 0 || ordered.isEmpty()) return
        val to = (from + direction).coerceIn(0, ordered.lastIndex)
        if (from == to) return
        val destinationCategory = ordered[to].category
        val moved = ordered.removeAt(from).copy(category = destinationCategory)
        ordered.add(to, moved)
        viewModelScope.launch { repository.reorder(ordered) }
    }
    fun moveCategory(category: ProductCategory, direction: Int) {
        val groups = state.value.groupedItems
        val categories = groups.keys.toMutableList()
        val from = categories.indexOf(category)
        if (from < 0 || categories.isEmpty()) return
        val to = (from + direction).coerceIn(0, categories.lastIndex)
        if (from == to) return
        categories.add(to, categories.removeAt(from))
        viewModelScope.launch { repository.reorder(categories.flatMap { groups[it].orEmpty() }) }
    }
    fun markAllIncomplete() { viewModelScope.launch { repository.markAllIncomplete()?.let { bulkChangeChannel.send(BulkListChange(BulkListAction.MARKED_INCOMPLETE, it)) } } }
    fun removeCompleted() { viewModelScope.launch { repository.removeCompleted()?.let { bulkChangeChannel.send(BulkListChange(BulkListAction.REMOVED_COMPLETED, it)) } } }
    fun undoBulkChange(change: BulkListChange) { viewModelScope.launch { repository.restoreCurrent(change.previousItems) } }
    fun updateActiveList(name: String, icon: SavedListIcon) {
        val id = state.value.activeList?.savedListId ?: return
        if (name.isNotBlank()) viewModelScope.launch { repository.updateList(id, name, icon) }
    }
    fun deleteActiveList() {
        val id = state.value.activeList?.savedListId ?: return
        viewModelScope.launch { repository.deleteSaved(id)?.let { deletedListChannel.send(it) } }
    }
    fun undoDeleteActiveList(snapshot: SavedListSnapshot) { viewModelScope.launch { repository.restoreSaved(snapshot); repository.activateList(snapshot.list.id) } }
    fun remove(item: ShoppingItem) { viewModelScope.launch { repository.remove(item.id); removalChannel.send(item) } }
    fun undoRemove(item: ShoppingItem) { viewModelScope.launch { repository.restoreItem(item) } }
    private fun refreshHistory() { viewModelScope.launch { history.value = repository.recent() to repository.frequent() } }
}
