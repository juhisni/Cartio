package fi.cartio.feature.savedlists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fi.cartio.core.localization.LocalStrings
import fi.cartio.core.model.SavedShoppingList

@Composable
fun SavedListsRoute(contentPadding: PaddingValues, onRestored: () -> Unit, viewModel: SavedListsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var dialog by remember { mutableStateOf<DialogState?>(null) }
    val strings = LocalStrings.current
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(viewModel, strings.undo) {
        viewModel.deletions.collect { snapshot ->
            if (snackbar.showSnackbar("${snapshot.list.name} ${strings.removed}", actionLabel = strings.undo, withDismissAction = true) == SnackbarResult.ActionPerformed) viewModel.undoDelete(snapshot)
        }
    }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(top = contentPadding.calculateTopPadding() + 16.dp, bottom = contentPadding.calculateBottomPadding() + 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(strings.saved, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
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
                }
            }
        }
        items(state.lists, key = { it.id }) { list ->
            SavedListCard(list, onRestore = { viewModel.restore(list.id); onRestored() }, onRename = { dialog = DialogState.Rename(list) }, onDelete = { viewModel.delete(list.id) })
        }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(bottom = contentPadding.calculateBottomPadding() + 16.dp))
    }
    dialog?.let { state -> NameDialog(initial = (state as? DialogState.Rename)?.list?.name.orEmpty(), title = if (state is DialogState.Save) strings.saveList else strings.rename, onDismiss = { dialog = null }, onConfirm = { name -> if (state is DialogState.Save) viewModel.save(name) else viewModel.rename((state as DialogState.Rename).list.id, name); dialog = null }) }
}

@Composable
private fun SavedListCard(list: SavedShoppingList, onRestore: () -> Unit, onRename: () -> Unit, onDelete: () -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }
    val strings = LocalStrings.current
    Card(Modifier.fillMaxWidth().padding(horizontal = 20.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(Modifier.fillMaxWidth().padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = .14f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.ShoppingCart, null, tint = MaterialTheme.colorScheme.primary) }
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(list.name, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(strings.itemCount.format(list.itemCount), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = onRestore) { Text(strings.restore) }
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
private fun NameDialog(initial: String, title: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf(initial) }; val strings = LocalStrings.current
    AlertDialog(onDismissRequest = onDismiss, shape = RoundedCornerShape(24.dp), title = { Text(title) }, text = { OutlinedTextField(name, { name = it }, label = { Text(strings.listName) }, singleLine = true, shape = RoundedCornerShape(14.dp)) }, confirmButton = { TextButton(enabled = name.isNotBlank(), onClick = { onConfirm(name) }) { Text(strings.save) } }, dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } })
}
