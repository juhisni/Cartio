package fi.cartio.feature.savedlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fi.cartio.core.model.SavedShoppingList
import fi.cartio.domain.repository.CartioRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SavedListsViewModel @Inject constructor(private val repository: CartioRepository) : ViewModel() {
    val lists: StateFlow<List<SavedShoppingList>> = repository.savedLists.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun save(name: String) { if (name.isNotBlank()) viewModelScope.launch { repository.save(name) } }
    fun restore(id: Long) { viewModelScope.launch { repository.restore(id) } }
    fun rename(id: Long, name: String) { if (name.isNotBlank()) viewModelScope.launch { repository.rename(id, name) } }
    fun delete(id: Long) { viewModelScope.launch { repository.deleteSaved(id) } }
}
