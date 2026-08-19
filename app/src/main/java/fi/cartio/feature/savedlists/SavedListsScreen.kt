package fi.cartio.feature.savedlists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Clear
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
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
    var dialogMode by rememberSaveable { mutableStateOf<String?>(null) }
    var dialogError by rememberSaveable { mutableStateOf(false) }
    var renameListId by rememberSaveable { mutableStateOf<Long?>(null) }
    val renameList = renameListId?.let { id -> state.lists.firstOrNull { it.id == id } }
    var confirmingDeleteId by rememberSaveable { mutableStateOf<Long?>(null) }
    val confirmingDelete = confirmingDeleteId?.let { id -> state.lists.firstOrNull { it.id == id } }
    val strings = LocalStrings.current
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(viewModel, strings.undo) {
        viewModel.deletions.collect { snapshot ->
            if (snackbar.showSnackbar("${snapshot.list.name} ${strings.removed}", actionLabel = strings.undo, withDismissAction = true, duration = SnackbarDuration.Short) == SnackbarResult.ActionPerformed) viewModel.undoDelete(snapshot)
        }
    }
    LaunchedEffect(viewModel, strings.duplicateCreated, strings.openDuplicate) {
        viewModel.duplications.collect { duplicate ->
            if (snackbar.showSnackbar(strings.duplicateCreated.format(duplicate.name), actionLabel = strings.openDuplicate, withDismissAction = true, duration = SnackbarDuration.Short) == SnackbarResult.ActionPerformed) {
                viewModel.restore(duplicate.id)
            }
        }
    }
    LaunchedEffect(viewModel) { viewModel.navigation.collect { onRestored() } }
    LaunchedEffect(viewModel, strings.listNameExists, dialogMode) { viewModel.nameConflicts.collect {
        if (dialogMode != null) dialogError = true else snackbar.showSnackbar(strings.listNameExists)
    } }
    LaunchedEffect(viewModel) { viewModel.updates.collect { dialogMode = null; renameListId = null; dialogError = false } }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(top = contentPadding.calculateTopPadding() + 16.dp, bottom = contentPadding.calculateBottomPadding() + 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
        item {
            CartioScreenHeader(strings.saved, Modifier.padding(horizontal = 20.dp)) {
                FilledTonalIconButton(onClick = { dialogError = false; dialogMode = "create" }, enabled = !state.isBusy, modifier = Modifier.size(48.dp)) { if (state.isBusy) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp) else Icon(Icons.Outlined.Add, strings.saveList) }
            }
        }
        if (state.hasSavedLists) item {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                placeholder = { Text(strings.searchLists) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.query.isNotEmpty()) IconButton(onClick = { viewModel.setQuery("") }) {
                        Icon(Icons.Outlined.Clear, contentDescription = strings.clearSearch)
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            )
        }
        if (state.lists.isEmpty()) item {
            if (state.hasSavedLists) {
                Column(Modifier.fillParentMaxWidth().padding(horizontal = 32.dp, vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(strings.noMatchingLists, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    TextButton(onClick = { viewModel.setQuery("") }, modifier = Modifier.padding(top = 8.dp)) { Text(strings.clearSearch) }
                }
            } else {
                Column(Modifier.fillParentMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.BookmarkBorder, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(52.dp))
                    Text(strings.savedEmptyTitle, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 18.dp))
                    Text(strings.savedEmptyBody, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                    Button(
                        onClick = { dialogError = false; dialogMode = "create" },
                        shape = RoundedCornerShape(15.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp).height(52.dp).testTag("saved_create_new_list"),
                    ) { Text(strings.createNewList) }
                }
            }
        }
        items(state.lists, key = { it.id }) { list ->
            SavedListCard(list, isActive = state.activeListId == list.id, isBusy = state.isBusy, onOpen = { viewModel.restore(list.id) }, onRename = { dialogError = false; renameListId = list.id; dialogMode = "rename" }, onDuplicate = { viewModel.duplicate(list.id, strings.duplicateListName.format(list.name)) }, onDelete = { confirmingDeleteId = list.id })
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
    dialogMode?.let { mode ->
        val existing = renameList.takeIf { mode == "rename" }
        if (mode == "rename" && existing == null) return@let
        NameDialog(
            initial = existing?.name.orEmpty(),
            initialIcon = existing?.icon ?: SavedListIcon.CART,
            title = if (mode == "create") strings.createNewList else strings.rename,
            errorMessage = if (dialogError) strings.listNameExists else null,
            isBusy = state.isBusy,
            onDismiss = { if (!state.isBusy) { dialogMode = null; renameListId = null; dialogError = false } },
            onConfirm = { name, icon ->
                dialogError = false
                if (mode == "create") viewModel.create(name, icon)
                else existing?.let { viewModel.update(it.id, name, icon) }
            },
        )
    }
    confirmingDelete?.let { list ->
        AlertDialog(
            onDismissRequest = { confirmingDeleteId = null },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = { Text(strings.deleteList) },
            text = { Text(strings.deleteListConfirmation.format(list.name)) },
            confirmButton = {
                TextButton(enabled = !state.isBusy, onClick = { confirmingDeleteId = null; viewModel.delete(list.id) }) {
                    Text(strings.delete)
                }
            },
            dismissButton = { TextButton(onClick = { confirmingDeleteId = null }) { Text(strings.cancel) } },
        )
    }
}

@Composable
private fun SavedListCard(list: SavedShoppingList, isActive: Boolean, isBusy: Boolean, onOpen: () -> Unit, onRename: () -> Unit, onDuplicate: () -> Unit, onDelete: () -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }
    val strings = LocalStrings.current
    Card(Modifier.fillMaxWidth().padding(horizontal = 20.dp).clickable(enabled = !isActive && !isBusy, onClick = onOpen), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
        Row(Modifier.fillMaxWidth().padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = .14f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Text(list.icon.symbol, style = MaterialTheme.typography.titleLarge) }
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(list.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(strings.listProgressText(list.itemCount, list.completedCount), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (isActive) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) { Text(strings.active, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)) }
            } else TextButton(onClick = onOpen, enabled = !isBusy) { Text(strings.restore) }
            Box {
                IconButton(onClick = { menuExpanded = true }, enabled = !isBusy) { Icon(Icons.Outlined.MoreVert, strings.moreOptions) }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(text = { Text(strings.rename) }, leadingIcon = { Icon(Icons.Outlined.Edit, null) }, onClick = { menuExpanded = false; onRename() })
                    DropdownMenuItem(text = { Text(strings.duplicateList) }, leadingIcon = { Icon(Icons.Outlined.ContentCopy, null) }, onClick = { menuExpanded = false; onDuplicate() })
                    DropdownMenuItem(text = { Text(strings.delete) }, leadingIcon = { Icon(Icons.Outlined.Delete, null) }, onClick = { menuExpanded = false; onDelete() })
                }
            }
        }
    }
}

@Composable
private fun NameDialog(initial: String, initialIcon: SavedListIcon, title: String, errorMessage: String?, isBusy: Boolean, onDismiss: () -> Unit, onConfirm: (String, SavedListIcon) -> Unit) {
    var name by rememberSaveable(initial) { mutableStateOf(initial) }
    var icon by rememberSaveable(initial) { mutableStateOf(initialIcon) }
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
                    OutlinedTextField(
                        name,
                        { name = it },
                        label = { Text(strings.listName) },
                        singleLine = true,
                        isError = errorMessage != null,
                        supportingText = errorMessage?.let { message -> { Text(message) } },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { if (name.isNotBlank() && !isBusy) onConfirm(name, icon) }),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        },
        confirmButton = { TextButton(enabled = name.isNotBlank() && !isBusy, onClick = { onConfirm(name, icon) }) { if (isBusy) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp) else Text(strings.save) } },
        dismissButton = { TextButton(enabled = !isBusy, onClick = onDismiss) { Text(strings.cancel) } },
    )
}
