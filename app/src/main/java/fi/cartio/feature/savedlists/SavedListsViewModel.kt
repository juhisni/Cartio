package fi.cartio.feature.savedlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
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
class SavedListsViewModel @Inject constructor(private val repository: CartioRepository, private val savedStateHandle: SavedStateHandle = SavedStateHandle()) : ViewModel() {
    private val query = savedStateHandle.getStateFlow("saved_list_query", "")
    private val busy = MutableStateFlow(false)
    val state: StateFlow<SavedListsUiState> = combine(repository.savedLists, repository.activeList, query, busy) { lists, active, search, working ->
        val filtered = if (search.isBlank()) lists else lists.filter { it.name.contains(search.trim(), ignoreCase = true) }
        SavedListsUiState(
            lists = filtered.sortedByDescending { it.id == active?.savedListId },
            query = search,
            hasSavedLists = lists.isNotEmpty(),
            activeListId = active?.savedListId,
            isBusy = working,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SavedListsUiState())
    private val deletionChannel = Channel<SavedListSnapshot>(Channel.BUFFERED)
    val deletions = deletionChannel.receiveAsFlow()
    private val duplicationChannel = Channel<DuplicatedList>(Channel.BUFFERED)
    val duplications = duplicationChannel.receiveAsFlow()
    private val navigationChannel = Channel<Unit>(Channel.BUFFERED)
    val navigation = navigationChannel.receiveAsFlow()
    private val nameConflictChannel = Channel<Unit>(Channel.BUFFERED)
    val nameConflicts = nameConflictChannel.receiveAsFlow()
    private val updateChannel = Channel<Unit>(Channel.BUFFERED)
    val updates = updateChannel.receiveAsFlow()
    fun setQuery(value: String) { savedStateHandle["saved_list_query"] = value }
    fun save(name: String) { if (name.isNotBlank()) viewModelScope.launch { repository.save(name) } }
    fun create(name: String, icon: SavedListIcon) { if (name.isNotBlank()) launchOperation {
        if (repository.createList(name, icon) != null) navigationChannel.send(Unit) else nameConflictChannel.send(Unit)
    } }
    fun restore(id: Long) { launchOperation { if (repository.restore(id)) navigationChannel.send(Unit) } }
    fun update(id: Long, name: String, icon: SavedListIcon) { if (name.isNotBlank()) launchOperation {
        if (repository.updateList(id, name, icon)) updateChannel.send(Unit) else nameConflictChannel.send(Unit)
    } }
    fun duplicate(id: Long, name: String) {
        if (name.isNotBlank()) launchOperation {
            repository.duplicateList(id, name)?.let { duplicationChannel.send(DuplicatedList(it, name.trim())) } ?: nameConflictChannel.send(Unit)
        }
    }
    fun delete(id: Long) { launchOperation { repository.deleteSaved(id)?.let { deletionChannel.send(it) } } }
    fun undoDelete(snapshot: SavedListSnapshot) { viewModelScope.launch { repository.restoreSaved(snapshot) } }
    private fun launchOperation(block: suspend () -> Unit) {
        if (busy.value) return
        busy.value = true
        viewModelScope.launch { try { block() } finally { busy.value = false } }
    }
}

data class DuplicatedList(val id: Long, val name: String)

data class SavedListsUiState(
    val lists: List<SavedShoppingList> = emptyList(),
    val query: String = "",
    val hasSavedLists: Boolean = false,
    val activeListId: Long? = null,
    val isBusy: Boolean = false,
)
