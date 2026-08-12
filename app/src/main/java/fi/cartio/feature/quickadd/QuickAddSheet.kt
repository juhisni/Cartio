package fi.cartio.feature.quickadd

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Snackbar
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fi.cartio.core.localization.LocalStrings
import fi.cartio.core.designsystem.productIcon
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
        Column(Modifier.fillMaxWidth().fillMaxHeight(.82f).imePadding().padding(horizontal = 20.dp)) {
            Text(text.addProduct, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 14.dp))
            OutlinedTextField(value = state.query, onValueChange = viewModel::setQuery, modifier = Modifier.fillMaxWidth().focusRequester(focus).testTag("quick_add_input"), shape = RoundedCornerShape(16.dp), singleLine = true, placeholder = { Text(text.searchHint) }, leadingIcon = { Icon(Icons.Outlined.Search, null) }, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { viewModel.add() }))
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                if (state.query.isBlank()) {
                    SuggestionGroup(text.recent, state.recent, viewModel::add)
                    SuggestionGroup(text.frequent, state.frequent, viewModel::add)
                } else {
                    SuggestionGroup(text.addProduct, state.suggestions, viewModel::add)
                    if (state.canAddQuery) {
                        CustomProductAction(
                            heading = text.productNotFound,
                            action = text.addTypedProduct.format(state.query.trim()),
                            onAdd = { viewModel.add() },
                        )
                    }
                }
            }
            SnackbarHost(snackbar, Modifier.fillMaxWidth().padding(bottom = 8.dp)) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    actionColor = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable private fun CustomProductAction(heading: String, action: String, onAdd: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 8.dp)) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Text(
            heading,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 14.dp),
        )
        TextButton(
            onClick = onAdd,
            modifier = Modifier.heightIn(min = 48.dp).testTag("add_typed_product"),
        ) {
            Icon(Icons.Rounded.Add, contentDescription = null)
            Text(action, modifier = Modifier.padding(start = 6.dp))
        }
    }
}

@Composable private fun SuggestionGroup(title: String, values: List<ProductSuggestion>, onAdd: (String) -> Unit) {
    if (values.isEmpty()) return
    Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 20.dp, bottom = 8.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { values.distinctBy { it.name }.forEach { suggestion -> AssistChip(onClick = { onAdd(suggestion.name) }, modifier = Modifier.heightIn(min = 44.dp).testTag("suggestion_${suggestion.name.lowercase().replace(' ', '_')}"), shape = RoundedCornerShape(14.dp), colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), label = { Text("${productIcon(suggestion.name, suggestion.category)}  ${suggestion.name}", maxLines = 2, overflow = TextOverflow.Ellipsis) }) } }
}
