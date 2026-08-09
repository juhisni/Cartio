package fi.cartio.feature.shoppinglist

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TextButton
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fi.cartio.core.localization.LocalStrings
import fi.cartio.core.designsystem.categoryIcon
import fi.cartio.core.designsystem.productIcon
import fi.cartio.core.designsystem.CartioScreenHeader
import fi.cartio.core.localization.categoryName
import fi.cartio.core.model.ProductCategory
import fi.cartio.core.model.ShoppingItem
import fi.cartio.core.model.ActiveShoppingList
import fi.cartio.core.model.SavedShoppingList
import fi.cartio.core.model.formatQuantity
import fi.cartio.ui.theme.CartioTheme
import fi.cartio.R
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListRoute(viewModel: ShoppingListViewModel, contentPadding: PaddingValues, onOpenSavedLists: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<ShoppingItem?>(null) }
    var creatingList by remember { mutableStateOf(false) }
    var switchingList by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val strings = LocalStrings.current
    LaunchedEffect(viewModel, strings.undo) {
        viewModel.removals.collect { item ->
            if (snackbar.showSnackbar("${item.name} ${strings.removed}", actionLabel = strings.undo, withDismissAction = true, duration = SnackbarDuration.Short) == SnackbarResult.ActionPerformed) viewModel.undoRemove(item)
        }
    }
    Box(Modifier.fillMaxSize()) {
        ShoppingListScreen(
            state, contentPadding, viewModel::toggle, viewModel::remove,
            onEdit = { editing = it },
            onCreateList = { creatingList = true },
            onOpenSavedLists = onOpenSavedLists,
            onSwitchList = { switchingList = true },
            onMoveItem = viewModel::moveItem,
            onMoveCategory = viewModel::moveCategory,
        )
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(bottom = contentPadding.calculateBottomPadding() + 76.dp))
    }
    editing?.let { item -> ProductEditorSheet(item, onDismiss = { editing = null }, onSave = { viewModel.update(it); editing = null }) }
    if (creatingList) CreateListSheet(onDismiss = { creatingList = false }, onCreate = { viewModel.createList(it); creatingList = false })
    if (switchingList) SwitchListSheet(
        active = state.activeList,
        lists = state.savedLists,
        onDismiss = { switchingList = false },
        onActivate = { viewModel.activateList(it); switchingList = false },
        onCreate = { switchingList = false; creatingList = true },
        onManage = { switchingList = false; onOpenSavedLists() },
    )
}

@Composable
fun ShoppingListScreen(
    state: ShoppingListUiState,
    contentPadding: PaddingValues,
    onToggle: (ShoppingItem) -> Unit,
    onRemove: (ShoppingItem) -> Unit,
    onEdit: (ShoppingItem) -> Unit = {},
    onCreateList: () -> Unit = {},
    onOpenSavedLists: () -> Unit = {},
    onSwitchList: () -> Unit = {},
    onMoveItem: (ShoppingItem, Int) -> Unit = { _, _ -> },
    onMoveCategory: (ProductCategory, Int) -> Unit = { _, _ -> },
) {
    var collapsedCategories by rememberSaveable { mutableStateOf(emptySet<String>()) }
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).animateContentSize(),
        contentPadding = PaddingValues(top = contentPadding.calculateTopPadding() + 16.dp, bottom = contentPadding.calculateBottomPadding() + 92.dp),
    ) {
        item {
            CartioScreenHeader(if (state.activeList == null) "Cartio" else LocalStrings.current.shoppingList, Modifier.padding(start = 20.dp, end = 12.dp, bottom = 10.dp))
        }
        if (state.activeList == null) {
            item { NoActiveListState(onCreateList, onOpenSavedLists) }
        } else {
            item { ActiveListCard(state.activeList, onSwitchList) }
        }
        if (state.activeList != null && state.groupedItems.isEmpty()) item {
            Column(Modifier.fillParentMaxSize().padding(horizontal = 48.dp, vertical = 72.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Image(painterResource(R.drawable.cartio_foreground), contentDescription = null, modifier = Modifier.size(110.dp))
                Spacer(Modifier.height(24.dp))
                Text(LocalStrings.current.emptyTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Text(LocalStrings.current.emptyBody, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth().padding(top = 10.dp), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
            }
        }
        if (state.activeList != null) state.groupedItems.forEach { (category, products) ->
            if (products.isNotEmpty()) {
                val collapsed = category.name in collapsedCategories
                item(key = "header-$category") {
                    CategoryHeader(category, products.count { !it.checked }, collapsed, onToggle = {
                        collapsedCategories = if (collapsed) collapsedCategories - category.name else collapsedCategories + category.name
                    }, onMove = { onMoveCategory(category, it) })
                }
                if (!collapsed) items(products, key = { it.id }) { product -> ProductRow(product, onToggle, onRemove, onEdit) { direction -> onMoveItem(product, direction) } }
            }
        }
    }
}

@Composable
private fun NoActiveListState(onCreate: () -> Unit, onOpenSaved: () -> Unit) {
    val strings = LocalStrings.current
    Column(Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 64.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Image(painterResource(R.drawable.cartio_foreground), contentDescription = null, modifier = Modifier.size(112.dp))
        Text(strings.whatWouldYouLike, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 24.dp))
        Button(onClick = onCreate, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(top = 24.dp).height(54.dp).testTag("create_new_list")) { Text(strings.createNewList) }
        OutlinedButton(onClick = onOpenSaved, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(top = 10.dp).height(54.dp)) { Text(strings.openSavedLists) }
        Row(Modifier.padding(top = 36.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Lock, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Text(strings.listsStayOnDevice, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun ActiveListCard(active: ActiveShoppingList, onSwitch: () -> Unit) {
    val strings = LocalStrings.current
    Card(
        onClick = onSwitch,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp).testTag("active_list_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(strings.currentList, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(active.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(strings.listProgress.format(active.itemCount, active.completedCount), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Outlined.KeyboardArrowDown, strings.switchList)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateListSheet(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    val strings = LocalStrings.current
    ModalBottomSheet(onDismissRequest = onDismiss, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)) {
        Column(Modifier.fillMaxWidth().imePadding().padding(horizontal = 20.dp, vertical = 8.dp).padding(bottom = 24.dp)) {
            Text(strings.createNewList, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
            OutlinedTextField(name, { name = it }, label = { Text(strings.listName) }, singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().testTag("new_list_name"))
            Button(onClick = { onCreate(name.trim()) }, enabled = name.isNotBlank(), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(54.dp).testTag("confirm_create_list")) { Text(strings.createList) }
            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text(strings.cancel) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwitchListSheet(active: ActiveShoppingList?, lists: List<SavedShoppingList>, onDismiss: () -> Unit, onActivate: (Long) -> Unit, onCreate: () -> Unit, onManage: () -> Unit) {
    val strings = LocalStrings.current
    val orderedLists = lists.sortedByDescending { if (it.id == active?.savedListId) 1 else 0 }
    ModalBottomSheet(onDismissRequest = onDismiss, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            Text(strings.switchList, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 14.dp))
            orderedLists.forEach { list ->
                val selected = list.id == active?.savedListId
                Surface(
                    onClick = { if (!selected) onActivate(list.id) },
                    shape = RoundedCornerShape(14.dp),
                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(list.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            val itemCount = if (selected) active.itemCount else list.itemCount
                            val completedCount = if (selected) active.completedCount else list.completedCount
                            Text(strings.listProgress.format(itemCount, completedCount), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (selected) Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface) { Text(strings.active, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)) }
                    }
                }
            }
            TextButton(onClick = onCreate, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Add, null); Text(strings.createNewList, modifier = Modifier.padding(start = 8.dp)) }
            TextButton(onClick = onManage, modifier = Modifier.fillMaxWidth()) { Text(strings.manageSavedLists) }
        }
    }
}

@Composable
private fun CategoryHeader(category: ProductCategory, count: Int, collapsed: Boolean, onToggle: () -> Unit, onMove: (Int) -> Unit) {
    val tint = categoryTint(category)
    val strings = LocalStrings.current
    var dragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(0f) }
    val scale by animateFloatAsState(if (dragging) 1.015f else 1f, label = "categoryDragScale")
    Row(
        Modifier.fillMaxWidth().testTag("category_${category.name}")
            .zIndex(if (dragging) 2f else 0f)
            .graphicsLayer { translationY = dragOffset; scaleX = scale; scaleY = scale }
            .shadow(if (dragging) 10.dp else 0.dp, RoundedCornerShape(14.dp))
            .reorderable(onMove) { active, offset -> dragging = active; dragOffset = offset }
            .background(if (dragging) tint.copy(alpha = .16f) else tint.copy(alpha = .07f), RoundedCornerShape(14.dp))
            .clickable(role = Role.Button, onClickLabel = if (collapsed) strings.expandCategory else strings.collapseCategory, onClick = onToggle)
            .padding(start = 20.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(categoryIcon(category), modifier = Modifier.padding(end = 9.dp, top = 12.dp, bottom = 12.dp))
        Text(categoryName(category), modifier = Modifier.weight(1f), color = tint, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
        Surface(shape = CircleShape, color = tint.copy(alpha = .14f)) { Text(count.toString(), color = tint, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)) }
        Icon(if (collapsed) Icons.Outlined.ExpandMore else Icons.Outlined.ExpandLess, contentDescription = null, tint = tint, modifier = Modifier.padding(start = 4.dp).size(24.dp))
    }
}

@Composable
private fun ProductRow(product: ShoppingItem, onToggle: (ShoppingItem) -> Unit, onRemove: (ShoppingItem) -> Unit, onEdit: (ShoppingItem) -> Unit, onMove: (Int) -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }
    var dragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(0f) }
    val scale by animateFloatAsState(if (dragging) 1.02f else 1f, label = "productDragScale")
    Row(
        Modifier.fillMaxWidth()
            .padding(start = 14.dp, end = 8.dp, top = 2.dp, bottom = 2.dp)
            .testTag("product_${product.normalizedName}")
            .zIndex(if (dragging) 3f else 0f)
            .graphicsLayer { translationY = dragOffset; scaleX = scale; scaleY = scale }
            .shadow(if (dragging) 12.dp else 0.dp, RoundedCornerShape(16.dp))
            .background(if (dragging) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(16.dp))
            .reorderable(onMove) { active, offset -> dragging = active; dragOffset = offset }
            .clickable(role = Role.Checkbox) { onToggle(product) }
            .alpha(if (product.checked) .58f else 1f)
            .padding(start = 16.dp, end = 4.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(productIcon(product.name, product.category), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(end = 12.dp))
        Column(Modifier.weight(1f).padding(vertical = 9.dp)) {
            Text(product.name, fontWeight = FontWeight.Medium, textDecoration = if (product.checked) TextDecoration.LineThrough else null, maxLines = 2, overflow = TextOverflow.Ellipsis)
            product.quantity?.let { Text(formatQuantity(it, product.unit), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        Box(
            Modifier.padding(11.dp).size(26.dp).background(if (product.checked) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape).border(1.5.dp, if (product.checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, CircleShape),
            contentAlignment = Alignment.Center,
        ) { Text(if (product.checked) "✓" else "", color = Color.White, fontWeight = FontWeight.Bold) }
        Box {
            IconButton(onClick = { menuExpanded = true }) { Icon(Icons.Outlined.MoreVert, contentDescription = LocalStrings.current.moreOptions, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(text = { Text(LocalStrings.current.editProduct) }, leadingIcon = { Icon(Icons.Outlined.Edit, null) }, onClick = { menuExpanded = false; onEdit(product) })
                DropdownMenuItem(text = { Text(LocalStrings.current.delete) }, leadingIcon = { Icon(Icons.Outlined.DeleteOutline, null) }, onClick = { menuExpanded = false; onRemove(product) })
            }
        }
    }
}

@Composable
private fun Modifier.reorderable(onMove: (Int) -> Unit, onDragVisual: (Boolean, Float) -> Unit): Modifier {
    val threshold = with(LocalDensity.current) { 44.dp.toPx() }
    var distance by remember { mutableStateOf(0f) }
    val strings = LocalStrings.current
    val haptics = LocalHapticFeedback.current
    val currentMove by rememberUpdatedState(onMove)
    val currentVisual by rememberUpdatedState(onDragVisual)
    return this
        .pointerInput(threshold) {
            detectDragGesturesAfterLongPress(
                onDragStart = { haptics.performHapticFeedback(HapticFeedbackType.LongPress); currentVisual(true, 0f) },
                onDragEnd = { distance = 0f; currentVisual(false, 0f) },
                onDragCancel = { distance = 0f; currentVisual(false, 0f) },
            ) { change, dragAmount ->
                change.consume()
                distance += dragAmount.y
                if (abs(distance) >= threshold) {
                    val direction = if (distance > 0) 1 else -1
                    currentMove(direction)
                    distance -= threshold * direction
                }
                currentVisual(true, distance)
            }
        }
        .semantics {
            customActions = listOf(
                CustomAccessibilityAction(strings.moveUp) { currentMove(-1); true },
                CustomAccessibilityAction(strings.moveDown) { currentMove(1); true },
            )
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductEditorSheet(item: ShoppingItem, onDismiss: () -> Unit, onSave: (ShoppingItem) -> Unit) {
    var name by remember(item.id) { mutableStateOf(item.name) }
    var quantity by remember(item.id) { mutableStateOf(item.quantity?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }.orEmpty()) }
    var unit by remember(item.id) { mutableStateOf(item.unit.orEmpty()) }
    var category by remember(item.id) { mutableStateOf(item.category) }
    val strings = LocalStrings.current
    ModalBottomSheet(onDismissRequest = onDismiss, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)) {
        Column(Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 28.dp)) {
            Text(strings.editProduct, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
            OutlinedTextField(name, { name = it }, label = { Text(strings.productName) }, singleLine = true, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(quantity, { value -> quantity = value.filter { it.isDigit() || it == '.' || it == ',' } }, label = { Text(strings.quantity) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), shape = RoundedCornerShape(14.dp), modifier = Modifier.weight(1f))
                OutlinedTextField(unit, { unit = it }, label = { Text(strings.unit) }, singleLine = true, shape = RoundedCornerShape(14.dp), modifier = Modifier.weight(1f))
            }
            Text(strings.category, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 18.dp, bottom = 6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProductCategory.entries.forEach { option -> FilterChip(selected = category == option, onClick = { category = option }, label = { Text(categoryName(option)) }) }
            }
            Button(onClick = { onSave(item.copy(name = name.trim(), quantity = quantity.replace(',', '.').toDoubleOrNull(), unit = unit.trim().ifBlank { null }, category = category)) }, enabled = name.isNotBlank(), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(52.dp)) { Text(strings.save) }
        }
    }
}

private fun categoryTint(category: ProductCategory) = when (category) { ProductCategory.DAIRY -> Color(0xFF2F79B9); ProductCategory.BREAD_GRAINS -> Color(0xFF9A6A12); ProductCategory.MEAT_FISH -> Color(0xFFC45151); ProductCategory.FROZEN -> Color(0xFF398FA7); ProductCategory.DRINKS -> Color(0xFF6D62B8); ProductCategory.HOUSEHOLD -> Color(0xFF7B6A57); else -> Color(0xFF287A36) }

@Preview(showBackground = true)
@Composable private fun ShoppingListPreview() { CartioTheme { ShoppingListScreen(ShoppingListUiState(mapOf(ProductCategory.FRUITS_VEGETABLES to listOf(ShoppingItem(1, "Banaanit", "banaanit", category = ProductCategory.FRUITS_VEGETABLES)), ProductCategory.DAIRY to listOf(ShoppingItem(2, "Maito", "maito", category = ProductCategory.DAIRY)))), PaddingValues(), {}, {}) } }
