package fi.cartio.feature.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fi.cartio.core.model.ProductCategory
import fi.cartio.core.model.ProductSuggestion
import fi.cartio.core.model.ShoppingItem
import fi.cartio.core.model.AppLanguage
import fi.cartio.domain.repository.CartioRepository
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
)

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
    val state: StateFlow<ShoppingListUiState> = combine(repository.items, query, history, suggestions) { items, text, usage, matches ->
        ShoppingListUiState(items.groupBy { it.category }, text, matches, usage.first, usage.second)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShoppingListUiState())
    private val feedbackChannel = Channel<String>(Channel.BUFFERED)
    val feedback = feedbackChannel.receiveAsFlow()
    private val removalChannel = Channel<ShoppingItem>(Channel.BUFFERED)
    val removals = removalChannel.receiveAsFlow()

    init { refreshHistory() }
    fun setQuery(value: String) { query.value = value }
    fun setLanguage(value: AppLanguage) { language.value = value }
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
    fun remove(item: ShoppingItem) { viewModelScope.launch { repository.remove(item.id); removalChannel.send(item) } }
    fun undoRemove(item: ShoppingItem) { viewModelScope.launch { repository.restoreItem(item) } }
    private fun refreshHistory() { viewModelScope.launch { history.value = repository.recent() to repository.frequent() } }
}
