package fi.cartio.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fi.cartio.core.localization.LocalStrings
import fi.cartio.core.designsystem.CartioScreenHeader
import fi.cartio.core.model.AppLanguage
import fi.cartio.core.model.ThemePreference
import fi.cartio.R
import androidx.core.content.pm.PackageInfoCompat

@Composable
fun SettingsRoute(viewModel: SettingsViewModel, contentPadding: PaddingValues) {
    val state by viewModel.settings.collectAsStateWithLifecycle(); val strings = LocalStrings.current
    val context = LocalContext.current
    val version = remember(context) {
        context.packageManager.getPackageInfo(context.packageName, 0).let { info ->
            info.versionName.orEmpty() to PackageInfoCompat.getLongVersionCode(info)
        }
    }
    LazyColumn(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = contentPadding.calculateTopPadding() + 16.dp, bottom = contentPadding.calculateBottomPadding() + 24.dp),
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
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(painterResource(R.drawable.cartio_foreground), contentDescription = null, modifier = Modifier.size(64.dp))
                    Column(Modifier.padding(start = 14.dp)) {
                        Text("Cartio", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(strings.aboutTagline, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        Text(strings.versionFormat.format(version.first, version.second), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(strings.aboutSummary, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
                HorizontalDivider(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Text(strings.privacyAndData, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                AboutInfoRow(Icons.Outlined.Lock, strings.privacySummary)
                AboutInfoRow(Icons.Outlined.Save, strings.localStorage, strings.localStorageBody)
                AboutInfoRow(Icons.Outlined.Save, strings.androidBackup, strings.androidBackupBody)
                AboutInfoRow(Icons.Outlined.CheckCircle, strings.permissions, strings.permissionsBody)
            }
        }
    }
}

@Composable
private fun AboutInfoRow(icon: ImageVector, title: String, body: String? = null) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Column(Modifier.padding(start = 12.dp)) {
            Text(title, fontWeight = if (body == null) FontWeight.Normal else FontWeight.SemiBold, color = if (body == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
            body?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp)) }
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
