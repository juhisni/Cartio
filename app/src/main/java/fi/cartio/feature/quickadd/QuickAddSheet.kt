package fi.cartio.feature.quickadd

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fi.cartio.core.localization.LocalStrings
import fi.cartio.core.model.ProductSuggestion
import fi.cartio.feature.shoppinglist.ShoppingListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun QuickAddSheet(viewModel: ShoppingListViewModel, onDismiss: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focus = remember { FocusRequester() }; val keyboard = LocalSoftwareKeyboardController.current
    val snackbar = remember { SnackbarHostState() }; val text = LocalStrings.current
    LaunchedEffect(Unit) { focus.requestFocus(); keyboard?.show() }
    LaunchedEffect(viewModel) { viewModel.feedback.collect { snackbar.showSnackbar("$it ${text.added}"); focus.requestFocus(); keyboard?.show() } }
    ModalBottomSheet(onDismissRequest = onDismiss, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp), containerColor = MaterialTheme.colorScheme.surface, dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle(width = 44.dp) }) {
        Column(Modifier.fillMaxWidth().heightIn(min = 340.dp).imePadding().padding(horizontal = 20.dp)) {
            Text(text.addProduct, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 14.dp))
            OutlinedTextField(value = state.query, onValueChange = viewModel::setQuery, modifier = Modifier.fillMaxWidth().focusRequester(focus).testTag("quick_add_input"), shape = RoundedCornerShape(16.dp), singleLine = true, placeholder = { Text(text.searchHint) }, leadingIcon = { Icon(Icons.Outlined.Search, null) }, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { viewModel.add() }))
            SuggestionGroup(if (state.query.isBlank()) text.recent else text.addProduct, if (state.query.isBlank()) state.recent else state.suggestions, viewModel::add)
            SuggestionGroup(text.frequent, state.frequent.ifEmpty { state.suggestions }, viewModel::add)
            SnackbarHost(snackbar, Modifier.fillMaxWidth().padding(bottom = 8.dp))
        }
    }
}

@Composable private fun SuggestionGroup(title: String, values: List<ProductSuggestion>, onAdd: (String) -> Unit) {
    if (values.isEmpty()) return
    Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 18.dp, bottom = 6.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { values.distinctBy { it.name }.forEach { suggestion -> AssistChip(onClick = { onAdd(suggestion.name) }, shape = RoundedCornerShape(12.dp), colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), label = { Text("${suggestionIcon(suggestion.name)}  ${suggestion.name}") }) } }
}

private fun suggestionIcon(name: String): String = when { name.contains("banaan", true) -> "🍌"; name.contains("maito", true) -> "🥛"; name.contains("leip", true) -> "🍞"; name.contains("juusto", true) -> "🧀"; name.contains("kurk", true) -> "🥒"; name.contains("pasta", true) -> "🍝"; else -> "🛒" }
