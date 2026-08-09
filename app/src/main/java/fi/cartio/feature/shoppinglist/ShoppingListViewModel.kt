package fi.cartio.feature.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fi.cartio.core.model.ProductCategory
import fi.cartio.core.model.ProductSuggestion
import fi.cartio.core.model.ShoppingItem
import fi.cartio.domain.repository.CartioRepository
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ShoppingListUiState(
    val groupedItems: Map<ProductCategory, List<ShoppingItem>> = emptyMap(),
    val query: String = "",
    val suggestions: List<ProductSuggestion> = emptyList(),
    val recent: List<ProductSuggestion> = emptyList(),
    val frequent: List<ProductSuggestion> = emptyList(),
)

@HiltViewModel
class ShoppingListViewModel @Inject constructor(private val repository: CartioRepository) : ViewModel() {
    private val query = MutableStateFlow("")
    private val history = MutableStateFlow(Pair(emptyList<ProductSuggestion>(), emptyList<ProductSuggestion>()))
    val state: StateFlow<ShoppingListUiState> = combine(repository.items, query, history) { items, text, usage ->
        ShoppingListUiState(items.groupBy { it.category }, text, repository.dictionarySuggestions(text), usage.first, usage.second)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShoppingListUiState())
    private val feedbackChannel = Channel<String>(Channel.BUFFERED)
    val feedback = feedbackChannel.receiveAsFlow()

    init { refreshHistory() }
    fun setQuery(value: String) { query.value = value }
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
    fun remove(item: ShoppingItem) { viewModelScope.launch { repository.remove(item.id) } }
    private fun refreshHistory() { viewModelScope.launch { history.value = repository.recent() to repository.frequent() } }
}
