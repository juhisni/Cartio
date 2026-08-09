package fi.cartio.feature.shoppinglist

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
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
import fi.cartio.R

@Composable
fun ShoppingListRoute(viewModel: ShoppingListViewModel, contentPadding: PaddingValues) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ShoppingListScreen(state, contentPadding, viewModel::toggle, viewModel::remove)
}

@Composable
fun ShoppingListScreen(state: ShoppingListUiState, contentPadding: PaddingValues, onToggle: (ShoppingItem) -> Unit, onRemove: (ShoppingItem) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).animateContentSize(),
        contentPadding = PaddingValues(top = contentPadding.calculateTopPadding() + 8.dp, bottom = contentPadding.calculateBottomPadding() + 92.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.cartio_foreground), contentDescription = null, modifier = Modifier.size(42.dp))
                Text(LocalStrings.current.shoppingList, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).padding(start = 10.dp))
                IconButton(onClick = {}) { Icon(Icons.Outlined.MoreVert, contentDescription = null) }
            }
        }
        if (state.groupedItems.isEmpty()) item {
            Column(Modifier.fillParentMaxSize().padding(horizontal = 48.dp, vertical = 72.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = .08f), modifier = Modifier.size(110.dp)) {
                    Box(contentAlignment = Alignment.Center) { Image(painterResource(R.drawable.cartio_foreground), contentDescription = null, modifier = Modifier.size(96.dp)) }
                }
                Spacer(Modifier.height(24.dp))
                Text(LocalStrings.current.emptyTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(LocalStrings.current.emptyBody, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp), style = MaterialTheme.typography.bodyLarge)
            }
        }
        ProductCategory.entries.forEach { category ->
            val products = state.groupedItems[category].orEmpty()
            if (products.isNotEmpty()) {
                item(key = "header-$category") { CategoryHeader(category, products.count { !it.checked }) }
                items(products, key = { it.id }) { product -> ProductRow(product, onToggle, onRemove) }
            }
        }
    }
}

@Composable
private fun CategoryHeader(category: ProductCategory, count: Int) {
    val tint = categoryTint(category)
    Row(
        Modifier.fillMaxWidth().testTag("category_${category.name}").background(tint.copy(alpha = .07f)).padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(categoryEmoji(category), modifier = Modifier.padding(end = 9.dp))
        Text(categoryName(category), modifier = Modifier.weight(1f), color = tint, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
        Surface(shape = CircleShape, color = tint.copy(alpha = .14f)) { Text(count.toString(), color = tint, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)) }
    }
}

@Composable
private fun ProductRow(product: ShoppingItem, onToggle: (ShoppingItem) -> Unit, onRemove: (ShoppingItem) -> Unit) {
    Row(
        Modifier.fillMaxWidth().testTag("product_${product.normalizedName}").clickable(role = Role.Checkbox) { onToggle(product) }.alpha(if (product.checked) .58f else 1f).padding(start = 20.dp, end = 8.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(productEmoji(product), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(end = 12.dp))
        Text(product.name, modifier = Modifier.weight(1f).padding(vertical = 11.dp), fontWeight = FontWeight.Medium, textDecoration = if (product.checked) TextDecoration.LineThrough else null)
        product.quantity?.let { Text("${it}${product.unit.orEmpty()}", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        Box(
            Modifier.size(26.dp).background(if (product.checked) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape).border(1.5.dp, if (product.checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, CircleShape).clickable(role = Role.Checkbox) { onToggle(product) },
            contentAlignment = Alignment.Center,
        ) { Text(if (product.checked) "✓" else "", color = Color.White, fontWeight = FontWeight.Bold) }
        IconButton(onClick = { onRemove(product) }) { Icon(Icons.Outlined.DeleteOutline, contentDescription = LocalStrings.current.delete, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

private fun categoryEmoji(category: ProductCategory) = when (category) { ProductCategory.FRUITS_VEGETABLES -> "🥬"; ProductCategory.DAIRY -> "🥛"; ProductCategory.BREAD_GRAINS -> "🌾"; ProductCategory.MEAT_FISH -> "🐟"; ProductCategory.FROZEN -> "❄️"; ProductCategory.PANTRY -> "🥫"; ProductCategory.DRINKS -> "🧃"; ProductCategory.HOUSEHOLD -> "🧻"; ProductCategory.OTHER -> "🛒" }
private fun productEmoji(item: ShoppingItem): String = when { "banaan" in item.normalizedName || "banana" in item.normalizedName -> "🍌"; "maito" in item.normalizedName || "milk" in item.normalizedName -> "🥛"; "toma" in item.normalizedName -> "🍅"; "leip" in item.normalizedName || "bread" in item.normalizedName -> "🍞"; "juusto" in item.normalizedName || "cheese" in item.normalizedName -> "🧀"; "lohi" in item.normalizedName || "salmon" in item.normalizedName -> "🐟"; else -> categoryEmoji(item.category) }
private fun categoryTint(category: ProductCategory) = when (category) { ProductCategory.DAIRY -> Color(0xFF2F79B9); ProductCategory.BREAD_GRAINS -> Color(0xFF9A6A12); ProductCategory.MEAT_FISH -> Color(0xFFC45151); ProductCategory.FROZEN -> Color(0xFF398FA7); ProductCategory.DRINKS -> Color(0xFF6D62B8); ProductCategory.HOUSEHOLD -> Color(0xFF7B6A57); else -> Color(0xFF287A36) }

@Preview(showBackground = true)
@Composable private fun ShoppingListPreview() { CartioTheme { ShoppingListScreen(ShoppingListUiState(mapOf(ProductCategory.FRUITS_VEGETABLES to listOf(ShoppingItem(1, "Banaanit", "banaanit", category = ProductCategory.FRUITS_VEGETABLES)), ProductCategory.DAIRY to listOf(ShoppingItem(2, "Maito", "maito", category = ProductCategory.DAIRY)))), PaddingValues(), {}, {}) } }
