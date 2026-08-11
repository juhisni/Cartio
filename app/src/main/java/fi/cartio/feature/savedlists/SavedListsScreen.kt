package fi.cartio.feature.savedlists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fi.cartio.core.localization.LocalStrings
import fi.cartio.core.designsystem.CartioScreenHeader
import fi.cartio.core.designsystem.SavedListIconPicker
import fi.cartio.core.model.SavedShoppingList
import fi.cartio.core.model.SavedListIcon

@Composable
fun SavedListsRoute(contentPadding: PaddingValues, onRestored: () -> Unit, viewModel: SavedListsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var dialog by remember { mutableStateOf<DialogState?>(null) }
    val strings = LocalStrings.current
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(viewModel, strings.undo) {
        viewModel.deletions.collect { snapshot ->
            if (snackbar.showSnackbar("${snapshot.list.name} ${strings.removed}", actionLabel = strings.undo, withDismissAction = true, duration = SnackbarDuration.Short) == SnackbarResult.ActionPerformed) viewModel.undoDelete(snapshot)
        }
    }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(top = contentPadding.calculateTopPadding() + 16.dp, bottom = contentPadding.calculateBottomPadding() + 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
        item {
            CartioScreenHeader(strings.saved, Modifier.padding(horizontal = 20.dp)) {
                FilledTonalIconButton(onClick = { dialog = DialogState.Save }, modifier = Modifier.size(48.dp)) { Icon(Icons.Outlined.Add, strings.saveList) }
            }
        }
        if (state.hasSavedLists) item {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                placeholder = { Text(strings.searchLists) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            )
        }
        if (state.lists.isEmpty()) item {
            if (state.hasSavedLists) {
                Text(strings.noMatchingLists, modifier = Modifier.fillMaxWidth().padding(40.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            } else {
                Column(Modifier.fillParentMaxWidth().padding(horizontal = 40.dp, vertical = 72.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(88.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = .1f), RoundedCornerShape(28.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.BookmarkBorder, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(42.dp))
                    }
                    Text(strings.savedEmptyTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 20.dp))
                    Text(strings.savedEmptyBody, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                    Button(
                        onClick = { dialog = DialogState.Save },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp).height(54.dp).testTag("saved_create_new_list"),
                    ) { Text(strings.createNewList) }
                }
            }
        }
        items(state.lists, key = { it.id }) { list ->
            SavedListCard(list, isActive = state.activeListId == list.id, onRestore = { viewModel.restore(list.id); onRestored() }, onRename = { dialog = DialogState.Rename(list) }, onDelete = { viewModel.delete(list.id) })
        }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(bottom = contentPadding.calculateBottomPadding() + 16.dp)) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                actionColor = MaterialTheme.colorScheme.primary,
                dismissActionContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    dialog?.let { state ->
        val existing = (state as? DialogState.Rename)?.list
        NameDialog(
            initial = existing?.name.orEmpty(),
            initialIcon = existing?.icon ?: SavedListIcon.CART,
            title = if (state is DialogState.Save) strings.createNewList else strings.rename,
            onDismiss = { dialog = null },
            onConfirm = { name, icon ->
                if (state is DialogState.Save) { viewModel.create(name, icon); onRestored() }
                else viewModel.update((state as DialogState.Rename).list.id, name, icon)
                dialog = null
            },
        )
    }
}

@Composable
private fun SavedListCard(list: SavedShoppingList, isActive: Boolean, onRestore: () -> Unit, onRename: () -> Unit, onDelete: () -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }
    val strings = LocalStrings.current
    Card(Modifier.fillMaxWidth().padding(horizontal = 20.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
        Row(Modifier.fillMaxWidth().padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = .14f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Text(list.icon.symbol, style = MaterialTheme.typography.titleLarge) }
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(list.name, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(strings.itemCount.format(list.itemCount), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (isActive) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) { Text(strings.active, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)) }
            } else TextButton(onClick = onRestore) { Text(strings.restore) }
            Box {
                IconButton(onClick = { menuExpanded = true }) { Icon(Icons.Outlined.MoreVert, strings.moreOptions) }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(text = { Text(strings.rename) }, leadingIcon = { Icon(Icons.Outlined.Edit, null) }, onClick = { menuExpanded = false; onRename() })
                    DropdownMenuItem(text = { Text(strings.delete) }, leadingIcon = { Icon(Icons.Outlined.Delete, null) }, onClick = { menuExpanded = false; onDelete() })
                }
            }
        }
    }
}

private sealed interface DialogState { data object Save : DialogState; data class Rename(val list: SavedShoppingList) : DialogState }

@Composable
private fun NameDialog(initial: String, initialIcon: SavedListIcon, title: String, onDismiss: () -> Unit, onConfirm: (String, SavedListIcon) -> Unit) {
    var name by remember { mutableStateOf(initial) }
    var icon by remember { mutableStateOf(initialIcon) }
    val strings = LocalStrings.current
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(title) },
        text = {
            Column {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    SavedListIconPicker(icon, strings.listIcon) { icon = it }
                    OutlinedTextField(name, { name = it }, label = { Text(strings.listName) }, singleLine = true, shape = RoundedCornerShape(14.dp), modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = { TextButton(enabled = name.isNotBlank(), onClick = { onConfirm(name, icon) }) { Text(strings.save) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } },
    )
}
