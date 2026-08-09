package fi.cartio.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fi.cartio.core.localization.LocalStrings
import fi.cartio.core.designsystem.CartioScreenHeader
import fi.cartio.core.model.AppLanguage
import fi.cartio.core.model.ThemePreference

@Composable
fun SettingsRoute(viewModel: SettingsViewModel, contentPadding: PaddingValues) {
    val state by viewModel.settings.collectAsStateWithLifecycle(); val strings = LocalStrings.current
    LazyColumn(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = contentPadding.calculateTopPadding() + 20.dp, bottom = contentPadding.calculateBottomPadding() + 24.dp),
    ) {
        item { CartioScreenHeader(strings.settings, Modifier.padding(bottom = 18.dp)) }
        item {
            SettingsCard(Icons.Outlined.Language, strings.language) {
                Choice(strings.finnish, state.language == AppLanguage.FINNISH) { viewModel.language(AppLanguage.FINNISH) }
                Choice(strings.english, state.language == AppLanguage.ENGLISH) { viewModel.language(AppLanguage.ENGLISH) }
            }
        }
        item {
            SettingsCard(Icons.Outlined.Palette, strings.theme) {
                Choice(strings.system, state.theme == ThemePreference.SYSTEM) { viewModel.theme(ThemePreference.SYSTEM) }
                Choice(strings.light, state.theme == ThemePreference.LIGHT) { viewModel.theme(ThemePreference.LIGHT) }
                Choice(strings.dark, state.theme == ThemePreference.DARK) { viewModel.theme(ThemePreference.DARK) }
            }
        }
        item {
            SettingsCard(Icons.Outlined.Info, strings.appInfo) {
                Text("Cartio 1.0", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                Text(strings.offlineDescription, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                Text(strings.catalogAttribution, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            }
        }
    }
}

@Composable private fun SettingsCard(icon: ImageVector, title: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(bottom = 14.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
        Row(Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp)); Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 10.dp)) }
        Column(Modifier.padding(bottom = 12.dp), content = { content() })
    }
}

@Composable private fun Choice(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(role = Role.RadioButton, onClick = onClick).padding(horizontal = 8.dp, vertical = 1.dp), verticalAlignment = Alignment.CenterVertically) { RadioButton(selected, onClick); Text(label) }
}
