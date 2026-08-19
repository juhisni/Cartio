package fi.cartio.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fi.cartio.core.localization.LocalStrings
import fi.cartio.core.designsystem.CartioScreenHeader
import fi.cartio.core.designsystem.CartioWordmark
import fi.cartio.core.model.AppLanguage
import fi.cartio.core.model.ThemePreference
import fi.cartio.R
import androidx.core.content.pm.PackageInfoCompat
import kotlinx.coroutines.launch

@Composable
fun SettingsRoute(viewModel: SettingsViewModel, contentPadding: PaddingValues, onOpenAbout: () -> Unit) {
    val state by viewModel.settings.collectAsStateWithLifecycle(); val strings = LocalStrings.current
    LazyColumn(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = contentPadding.calculateTopPadding() + 16.dp, bottom = contentPadding.calculateBottomPadding() + 24.dp),
    ) {
        item { CartioScreenHeader(strings.settings, Modifier.padding(bottom = 10.dp)) }
        item {
            SettingsCard(Icons.Outlined.Language, strings.language) {
                Choice(strings.english, state.language == AppLanguage.ENGLISH) { viewModel.language(AppLanguage.ENGLISH) }
                Choice(strings.finnish, state.language == AppLanguage.FINNISH) { viewModel.language(AppLanguage.FINNISH) }
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
            Card(
                onClick = onOpenAbout,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    Text(strings.appInfo, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f).padding(start = 10.dp))
                    Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun AboutCartioScreen(contentPadding: PaddingValues, onBack: () -> Unit) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val openUri: (String) -> Unit = { uri ->
        runCatching { uriHandler.openUri(uri) }.onFailure { scope.launch { snackbar.showSnackbar(strings.linkUnavailable) } }
    }
    val version = remember(context) {
        context.packageManager.getPackageInfo(context.packageName, 0).let { info ->
            info.versionName.orEmpty() to PackageInfoCompat.getLongVersionCode(info)
        }
    }
    Box(Modifier.fillMaxSize()) {
      LazyColumn(
          Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
          contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = contentPadding.calculateTopPadding() + 16.dp, bottom = contentPadding.calculateBottomPadding() + 24.dp),
      ) {
        item {
            Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = strings.back) }
                Text(strings.appInfo, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
            }
        }
        item {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(painterResource(R.drawable.cartio_foreground), contentDescription = null, modifier = Modifier.size(64.dp))
                    Column(Modifier.padding(start = 14.dp)) {
                        CartioWordmark(style = MaterialTheme.typography.titleLarge)
                        Text(strings.aboutTagline, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        Text(strings.versionFormat.format(version.first, version.second), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(strings.aboutSummary, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
                HorizontalDivider(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Text(strings.developerAndSupport, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                AboutInfoRow(Icons.Outlined.Person, strings.developedBy, "Juha-Matti Niiranen")
                AboutInfoRow(Icons.Outlined.Email, strings.contactSupport, "cartiosupport@gmail.com") {
                    openUri("mailto:cartiosupport@gmail.com?subject=Cartio%20support")
                }
                HorizontalDivider(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Text(strings.privacyAndData, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                AboutInfoRow(Icons.Outlined.Policy, strings.privacyPolicy, strings.privacyPolicyBody) {
                    openUri("https://juhisni.github.io/Cartio/privacy/")
                }
                AboutInfoRow(Icons.Outlined.Lock, strings.privacySummary)
                AboutInfoRow(Icons.Outlined.Save, strings.localStorage, strings.localStorageBody)
                AboutInfoRow(Icons.Outlined.Save, strings.androidBackup, strings.androidBackupBody)
                AboutInfoRow(Icons.Outlined.CheckCircle, strings.permissions, strings.permissionsBody)
                HorizontalDivider(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Text(strings.legalAndLicenses, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                AboutInfoRow(Icons.Outlined.Gavel, strings.copyrightNotice, strings.allRightsReserved)
                AboutInfoRow(Icons.Outlined.Policy, strings.legalNotices, strings.legalNoticesBody) {
                    openUri("https://juhisni.github.io/Cartio/legal/")
                }
            }
        }
      }
      SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(bottom = contentPadding.calculateBottomPadding() + 16.dp))
    }
}

@Composable
private fun AboutInfoRow(icon: ImageVector, title: String, body: String? = null, onClick: (() -> Unit)? = null) {
    val modifier = if (onClick == null) Modifier else Modifier.clickable(role = Role.Button, onClick = onClick)
    Row(modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Column(Modifier.padding(start = 12.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = if (body == null) FontWeight.Normal else FontWeight.SemiBold, color = if (body == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
            body?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp)) }
        }
    }
}

@Composable private fun SettingsCard(icon: ImageVector, title: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(bottom = 12.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
        Row(Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp)); Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 10.dp)) }
        Column(Modifier.padding(bottom = 12.dp), content = { content() })
    }
}

@Composable private fun Choice(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(role = Role.RadioButton, onClick = onClick).padding(start = 24.dp, end = 8.dp).heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected, onClick = null)
        Text(label, modifier = Modifier.padding(start = 8.dp))
    }
}
