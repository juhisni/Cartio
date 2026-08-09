package fi.cartio.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fi.cartio.core.localization.AppSettings
import fi.cartio.core.localization.SettingsRepository
import fi.cartio.core.model.AppLanguage
import fi.cartio.core.model.ThemePreference
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(private val repository: SettingsRepository) : ViewModel() {
    val settings: StateFlow<AppSettings> = repository.settings.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())
    fun language(value: AppLanguage) { viewModelScope.launch { repository.setLanguage(value) } }
    fun theme(value: ThemePreference) { viewModelScope.launch { repository.setTheme(value) } }
}
