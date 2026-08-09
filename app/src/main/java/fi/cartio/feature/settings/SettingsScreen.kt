package fi.cartio.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fi.cartio.core.localization.LocalStrings
import fi.cartio.core.model.AppLanguage
import fi.cartio.core.model.ThemePreference

@Composable fun SettingsRoute(viewModel: SettingsViewModel, contentPadding: PaddingValues) {
    val state by viewModel.settings.collectAsStateWithLifecycle(); val s = LocalStrings.current
    Column(Modifier.fillMaxSize().padding(top = contentPadding.calculateTopPadding() + 20.dp, bottom = contentPadding.calculateBottomPadding()).padding(horizontal = 20.dp)) {
        Text(s.settings, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        SectionTitle(s.language)
        Choice(s.finnish, state.language == AppLanguage.FINNISH) { viewModel.language(AppLanguage.FINNISH) }
        Choice(s.english, state.language == AppLanguage.ENGLISH) { viewModel.language(AppLanguage.ENGLISH) }
        HorizontalDivider()
        SectionTitle(s.theme)
        Choice(s.system, state.theme == ThemePreference.SYSTEM) { viewModel.theme(ThemePreference.SYSTEM) }
        Choice(s.light, state.theme == ThemePreference.LIGHT) { viewModel.theme(ThemePreference.LIGHT) }
        Choice(s.dark, state.theme == ThemePreference.DARK) { viewModel.theme(ThemePreference.DARK) }
        HorizontalDivider(); SectionTitle(s.appInfo); Text("Cartio 1.0\n${if (s.main == "Päänäkymä") "Nopea, rauhallinen ja täysin offline." else "Fast, calm, and completely offline."}", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
@Composable private fun SectionTitle(value: String) { Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)) }
@Composable private fun Choice(label: String, selected: Boolean, onClick: () -> Unit) { Row(Modifier.fillMaxWidth().clickable(role = Role.RadioButton, onClick = onClick).padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) { RadioButton(selected, onClick); Text(label) } }
