package fi.cartio

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.CircleShape
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import fi.cartio.core.localization.LocalStrings
import fi.cartio.core.localization.strings
import fi.cartio.core.model.ThemePreference
import fi.cartio.feature.quickadd.QuickAddSheet
import fi.cartio.feature.savedlists.SavedListsRoute
import fi.cartio.feature.settings.SettingsRoute
import fi.cartio.feature.settings.SettingsViewModel
import fi.cartio.feature.shoppinglist.ShoppingListRoute
import fi.cartio.feature.shoppinglist.ShoppingListViewModel
import fi.cartio.ui.theme.CartioTheme

private enum class Destination(val route: String, val icon: ImageVector) { Main("main", Icons.Outlined.Home), Saved("saved", Icons.Outlined.BookmarkBorder), Settings("settings", Icons.Outlined.Settings) }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartioApp(settingsViewModel: SettingsViewModel = hiltViewModel(), shoppingViewModel: ShoppingListViewModel = hiltViewModel()) {
    val preferences by settingsViewModel.settings.collectAsStateWithLifecycle()
    val dark = when (preferences.theme) { ThemePreference.DARK -> true; ThemePreference.LIGHT -> false; ThemePreference.SYSTEM -> isSystemInDarkTheme() }
    CartioTheme(darkTheme = dark) {
        CompositionLocalProvider(LocalStrings provides strings(preferences.language)) {
            val nav = rememberNavController()
            val backStack by nav.currentBackStackEntryAsState()
            val current = backStack?.destination?.route ?: Destination.Main.route
            var quickAdd by remember { mutableStateOf(false) }
            val labels = LocalStrings.current
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    NavigationBar(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars), containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                        Destination.entries.forEach { destination ->
                            val label = when (destination) { Destination.Main -> labels.main; Destination.Saved -> labels.saved; Destination.Settings -> labels.settings }
                            NavigationBarItem(selected = current == destination.route, onClick = { nav.navigate(destination.route) { popUpTo(nav.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } }, icon = { Icon(destination.icon, contentDescription = label) }, label = { Text(label) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, selectedTextColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = .12f)))
                        }
                    }
                },
                floatingActionButton = {
                    if (current == Destination.Main.route) FloatingActionButton(onClick = { quickAdd = true }, modifier = Modifier.size(64.dp).shadow(12.dp, CircleShape).testTag("open_quick_add"), shape = CircleShape, containerColor = MaterialTheme.colorScheme.primary) { Icon(Icons.Rounded.Add, contentDescription = labels.addProduct, modifier = Modifier.size(32.dp)) }
                },
                floatingActionButtonPosition = FabPosition.Center,
            ) { padding ->
                Box(Modifier.fillMaxSize()) {
                    NavHost(navController = nav, startDestination = Destination.Main.route) {
                        composable(Destination.Main.route) { ShoppingListRoute(shoppingViewModel, padding) }
                        composable(Destination.Saved.route) { SavedListsRoute(contentPadding = padding, onRestored = { nav.navigate(Destination.Main.route) }) }
                        composable(Destination.Settings.route) { SettingsRoute(settingsViewModel, padding) }
                    }
                    if (quickAdd) QuickAddSheet(viewModel = shoppingViewModel, onDismiss = { quickAdd = false })
                }
            }
        }
    }
}
