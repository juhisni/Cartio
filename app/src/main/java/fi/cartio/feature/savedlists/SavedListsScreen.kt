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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fi.cartio.core.localization.LocalStrings
import fi.cartio.core.model.SavedShoppingList

@Composable
fun SavedListsRoute(contentPadding: PaddingValues, onRestored: () -> Unit, viewModel: SavedListsViewModel = hiltViewModel()) {
    val lists by viewModel.lists.collectAsStateWithLifecycle()
    var dialog by remember { mutableStateOf<DialogState?>(null) }
    val strings = LocalStrings.current
    LazyColumn(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(top = contentPadding.calculateTopPadding() + 16.dp, bottom = contentPadding.calculateBottomPadding() + 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(strings.saved, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Button(onClick = { dialog = DialogState.Save }, shape = RoundedCornerShape(14.dp)) { Icon(Icons.Outlined.Add, null); Text(strings.saveList, modifier = Modifier.padding(start = 6.dp)) }
            }
        }
        if (lists.isEmpty()) item { Text(strings.emptyTitle, modifier = Modifier.padding(40.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(lists, key = { it.id }) { list ->
            Card(Modifier.fillMaxWidth().padding(horizontal = 20.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = .14f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.ShoppingCart, null, tint = MaterialTheme.colorScheme.primary) }
                    Column(Modifier.weight(1f).padding(start = 12.dp)) { Text(list.name, fontWeight = FontWeight.SemiBold); Text("${list.itemCount} ${if (strings.main == "Päänäkymä") "tuotetta" else "items"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    TextButton(onClick = { viewModel.restore(list.id); onRestored() }) { Text(strings.restore) }
                    IconButton(onClick = { dialog = DialogState.Rename(list) }) { Icon(Icons.Outlined.Edit, strings.rename) }
                    IconButton(onClick = { viewModel.delete(list.id) }) { Icon(Icons.Outlined.Delete, strings.delete) }
                }
            }
        }
    }
    dialog?.let { state -> NameDialog(initial = (state as? DialogState.Rename)?.list?.name.orEmpty(), title = if (state is DialogState.Save) strings.saveList else strings.rename, onDismiss = { dialog = null }, onConfirm = { name -> if (state is DialogState.Save) viewModel.save(name) else viewModel.rename((state as DialogState.Rename).list.id, name); dialog = null }) }
}

private sealed interface DialogState { data object Save : DialogState; data class Rename(val list: SavedShoppingList) : DialogState }

@Composable
private fun NameDialog(initial: String, title: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf(initial) }; val strings = LocalStrings.current
    AlertDialog(onDismissRequest = onDismiss, shape = RoundedCornerShape(24.dp), title = { Text(title) }, text = { OutlinedTextField(name, { name = it }, label = { Text(strings.listName) }, singleLine = true, shape = RoundedCornerShape(14.dp)) }, confirmButton = { TextButton(enabled = name.isNotBlank(), onClick = { onConfirm(name) }) { Text(strings.save) } }, dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } })
}
