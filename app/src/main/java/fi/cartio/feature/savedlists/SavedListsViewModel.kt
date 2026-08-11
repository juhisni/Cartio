package fi.cartio.feature.savedlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fi.cartio.core.model.SavedShoppingList
import fi.cartio.core.model.SavedListSnapshot
import fi.cartio.core.model.SavedListIcon
import fi.cartio.domain.repository.CartioRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

@HiltViewModel
class SavedListsViewModel @Inject constructor(private val repository: CartioRepository) : ViewModel() {
    private val query = MutableStateFlow("")
    val state: StateFlow<SavedListsUiState> = combine(repository.savedLists, repository.activeList, query) { lists, active, search ->
        SavedListsUiState(
            lists = if (search.isBlank()) lists else lists.filter { it.name.contains(search.trim(), ignoreCase = true) },
            query = search,
            hasSavedLists = lists.isNotEmpty(),
            activeListId = active?.savedListId,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SavedListsUiState())
    private val deletionChannel = Channel<SavedListSnapshot>(Channel.BUFFERED)
    val deletions = deletionChannel.receiveAsFlow()
    fun setQuery(value: String) { query.value = value }
    fun save(name: String) { if (name.isNotBlank()) viewModelScope.launch { repository.save(name) } }
    fun create(name: String, icon: SavedListIcon) { if (name.isNotBlank()) viewModelScope.launch { repository.createList(name, icon) } }
    fun restore(id: Long) { viewModelScope.launch { repository.restore(id) } }
    fun update(id: Long, name: String, icon: SavedListIcon) { if (name.isNotBlank()) viewModelScope.launch { repository.updateList(id, name, icon) } }
    fun duplicate(id: Long, name: String) { if (name.isNotBlank()) viewModelScope.launch { repository.duplicateList(id, name) } }
    fun delete(id: Long) { viewModelScope.launch { repository.deleteSaved(id)?.let { deletionChannel.send(it) } } }
    fun undoDelete(snapshot: SavedListSnapshot) { viewModelScope.launch { repository.restoreSaved(snapshot) } }
}

data class SavedListsUiState(
    val lists: List<SavedShoppingList> = emptyList(),
    val query: String = "",
    val hasSavedLists: Boolean = false,
    val activeListId: Long? = null,
)
