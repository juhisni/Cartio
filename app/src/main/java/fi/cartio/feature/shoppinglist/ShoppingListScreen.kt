package fi.cartio.feature.shoppinglist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fi.cartio.core.localization.LocalStrings
import fi.cartio.core.localization.categoryName
import fi.cartio.core.model.ProductCategory
import fi.cartio.core.model.ShoppingItem
import fi.cartio.ui.theme.CartioTheme

@Composable fun ShoppingListRoute(viewModel: ShoppingListViewModel, contentPadding: PaddingValues) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ShoppingListScreen(state, contentPadding, viewModel::toggle, viewModel::remove)
}

@Composable fun ShoppingListScreen(state: ShoppingListUiState, contentPadding: PaddingValues, onToggle: (ShoppingItem) -> Unit, onRemove: (ShoppingItem) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(top = contentPadding.calculateTopPadding() + 16.dp, bottom = contentPadding.calculateBottomPadding() + 88.dp)) {
        item { Text(LocalStrings.current.shoppingList, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) }
        if (state.groupedItems.isEmpty()) item {
            Column(Modifier.fillParentMaxSize().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("🛒", style = MaterialTheme.typography.displayMedium)
                Spacer(Modifier.height(16.dp)); Text(LocalStrings.current.emptyTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text(LocalStrings.current.emptyBody, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
            }
        }
        ProductCategory.entries.forEach { category ->
            val products = state.groupedItems[category].orEmpty()
            if (products.isNotEmpty()) {
                item(key = "header-$category") {
                    Row(Modifier.fillMaxWidth().testTag("category_${category.name}").padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(categoryName(category), modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text(products.count { !it.checked }.toString(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                items(products, key = { it.id }) { product ->
                    Row(Modifier.fillMaxWidth().testTag("product_${product.normalizedName}").clickable(role = Role.Checkbox) { onToggle(product) }.padding(start = 12.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = product.checked, onCheckedChange = { onToggle(product) })
                        Text(product.name, modifier = Modifier.weight(1f).padding(vertical = 14.dp), color = if (product.checked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface, textDecoration = if (product.checked) TextDecoration.LineThrough else null)
                        product.quantity?.let { Text("${it}${product.unit.orEmpty()}") }
                        IconButton(onClick = { onRemove(product) }) { Icon(Icons.Outlined.Delete, contentDescription = LocalStrings.current.delete) }
                    }
                    HorizontalDivider(Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
        }
    }
}

@Preview(showBackground = true) @Composable private fun ShoppingListPreview() { CartioTheme { ShoppingListScreen(ShoppingListUiState(mapOf(ProductCategory.DAIRY to listOf(ShoppingItem(1, "Maito", "maito", category = ProductCategory.DAIRY)))), PaddingValues(), {}, {}) } }
