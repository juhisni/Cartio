package fi.cartio.core.localization

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import fi.cartio.core.model.AppLanguage
import fi.cartio.core.model.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext

private val Context.dataStore by preferencesDataStore("settings")

data class AppSettings(val language: AppLanguage = AppLanguage.ENGLISH, val theme: ThemePreference = ThemePreference.SYSTEM)

class SettingsRepository @Inject constructor(@param:ApplicationContext private val context: Context) {
    private val language = stringPreferencesKey("language")
    private val theme = stringPreferencesKey("theme")
    val settings: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        AppSettings(
            language = preferences[language]?.let(AppLanguage::valueOf) ?: AppLanguage.ENGLISH,
            theme = preferences[theme]?.let(ThemePreference::valueOf) ?: ThemePreference.SYSTEM,
        )
    }
    suspend fun setLanguage(value: AppLanguage) { context.dataStore.edit { it[language] = value.name } }
    suspend fun setTheme(value: ThemePreference) { context.dataStore.edit { it[theme] = value.name } }
}
