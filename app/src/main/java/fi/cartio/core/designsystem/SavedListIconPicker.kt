package fi.cartio.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import fi.cartio.core.model.SavedListIcon

@Composable
fun SavedListIconPicker(selected: SavedListIcon, label: String, onSelected: (SavedListIcon) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        FilledTonalIconButton(
            onClick = { expanded = true },
            modifier = Modifier.size(56.dp).testTag("list_icon_selector").semantics { contentDescription = label },
        ) {
            Text(selected.symbol, style = MaterialTheme.typography.titleLarge)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SavedListIcon.entries.forEach { icon ->
                DropdownMenuItem(
                    text = { Text(icon.symbol, style = MaterialTheme.typography.titleLarge) },
                    trailingIcon = { if (icon == selected) Text("✓", color = MaterialTheme.colorScheme.primary) },
                    onClick = { onSelected(icon); expanded = false },
                    modifier = Modifier.testTag("list_icon_${icon.name.lowercase()}").semantics {
                        contentDescription = "$label: ${icon.symbol}"
                        this.selected = icon == selected
                    },
                )
            }
        }
    }
}
