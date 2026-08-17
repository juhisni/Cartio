package fi.cartio

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
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
import fi.cartio.feature.settings.AboutCartioScreen
import fi.cartio.feature.settings.SettingsViewModel
import fi.cartio.feature.shoppinglist.ShoppingListRoute
import fi.cartio.feature.shoppinglist.ShoppingListViewModel
import fi.cartio.ui.theme.CartioTheme
import kotlinx.coroutines.delay

private enum class Destination(val route: String, val icon: ImageVector) { Main("main", Icons.Outlined.Home), Saved("saved", Icons.Outlined.BookmarkBorder), Settings("settings", Icons.Outlined.Settings) }
private const val AboutRoute = "about"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartioApp(
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    shoppingViewModel: ShoppingListViewModel = hiltViewModel(),
    showBrandedSplash: Boolean = true,
    onSplashFinished: () -> Unit = {},
    onDarkThemeChanged: (Boolean) -> Unit = {},
) {
    val preferences by settingsViewModel.settings.collectAsStateWithLifecycle()
    val dark = when (preferences.theme) { ThemePreference.DARK -> true; ThemePreference.LIGHT -> false; ThemePreference.SYSTEM -> isSystemInDarkTheme() }
    SideEffect { onDarkThemeChanged(dark) }
    var showSplash by remember { mutableStateOf(showBrandedSplash) }
    LaunchedEffect(showBrandedSplash) {
        if (showBrandedSplash) {
            delay(900)
            showSplash = false
            onSplashFinished()
        }
    }
    if (showSplash) {
        CartioTheme(darkTheme = dark) {
            Image(
                painter = painterResource(if (dark) R.drawable.cartio_splash_dark else R.drawable.cartio_splash_light),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            )
        }
        return
    }
    val shoppingState by shoppingViewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(preferences.language) { shoppingViewModel.setLanguage(preferences.language) }
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
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                        Destination.entries.forEach { destination ->
                            val label = when (destination) { Destination.Main -> labels.main; Destination.Saved -> labels.saved; Destination.Settings -> labels.settings }
                            NavigationBarItem(selected = current == destination.route || (destination == Destination.Settings && current == AboutRoute), onClick = { nav.navigate(destination.route) { popUpTo(nav.graph.findStartDestination().id); launchSingleTop = true } }, icon = { Icon(destination.icon, contentDescription = label) }, label = { Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, selectedTextColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = .12f)))
                        }
                    }
                },
                floatingActionButton = {
                    if (current == Destination.Main.route && shoppingState.activeList != null) FloatingActionButton(onClick = { quickAdd = true }, modifier = Modifier.size(56.dp).shadow(6.dp, CircleShape).testTag("open_quick_add"), shape = CircleShape, containerColor = MaterialTheme.colorScheme.primary) { Icon(Icons.Rounded.Add, contentDescription = labels.addProduct, modifier = Modifier.size(28.dp)) }
                },
                floatingActionButtonPosition = FabPosition.Center,
            ) { padding ->
                Box(Modifier.fillMaxSize()) {
                    NavHost(navController = nav, startDestination = Destination.Main.route) {
                        composable(Destination.Main.route) {
                            ShoppingListRoute(
                                viewModel = shoppingViewModel,
                                contentPadding = padding,
                                onOpenSavedLists = { nav.navigate(Destination.Saved.route) },
                                onAddProduct = { quickAdd = true },
                                shouldShowReorderHint = !preferences.reorderHintShown,
                                onReorderHintShown = settingsViewModel::markReorderHintShown,
                            )
                        }
                        composable(Destination.Saved.route) { SavedListsRoute(contentPadding = padding, onRestored = { nav.navigate(Destination.Main.route) }) }
                        composable(Destination.Settings.route) { SettingsRoute(settingsViewModel, padding, onOpenAbout = { nav.navigate(AboutRoute) }) }
                        composable(AboutRoute) { AboutCartioScreen(padding, onBack = { nav.popBackStack() }) }
                    }
                    if (quickAdd) QuickAddSheet(viewModel = shoppingViewModel, onDismiss = { quickAdd = false })
                }
            }
        }
    }
}
