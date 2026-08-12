package fi.cartio.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

@Composable
fun CartioWordmark(
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineSmall,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    var stemBounds by remember { mutableStateOf<Rect?>(null) }
    val dotColor = MaterialTheme.colorScheme.primary
    Text(
        text = "Cartıo",
        style = style,
        fontWeight = FontWeight.Bold,
        color = color,
        maxLines = 1,
        onTextLayout = { layout -> stemBounds = layout.getBoundingBox(4) },
        modifier = modifier
            .clearAndSetSemantics { contentDescription = "Cartio" }
            .drawWithContent {
                drawContent()
                stemBounds?.let { bounds ->
                    val radius = (bounds.width * 0.34f).coerceAtLeast(1.5f)
                    drawIntoCanvas {
                        drawCircle(
                            color = dotColor,
                            radius = radius,
                            center = androidx.compose.ui.geometry.Offset(bounds.center.x, bounds.top + radius * 1.9f),
                        )
                    }
                }
            },
    )
}
