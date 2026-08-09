package fi.cartio.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CartioLightGreen, secondary = CartioGreen, tertiary = CartioForest,
    background = Color(0xFF101713), surface = Color(0xFF17201A), onSurface = Color(0xFFF2F4F2),
    primaryContainer = Color(0xFF214D29), onPrimaryContainer = Color(0xFFDDF3DF),
    secondaryContainer = Color(0xFF1C3C24), onSecondaryContainer = Color(0xFFE2F2E2),
    surfaceContainerLowest = Color(0xFF0C120E), surfaceContainerLow = Color(0xFF151D17),
    surfaceContainer = Color(0xFF1A241D), surfaceVariant = Color(0xFF263229),
    outlineVariant = Color(0xFF3D4D40),
)

private val LightColorScheme = lightColorScheme(
    primary = CartioGreen, secondary = CartioForest, tertiary = CartioLightGreen,
    background = CartioCream, surface = Color.White, onPrimary = Color.White,
    onBackground = CartioInk, onSurface = CartioInk,
    onSecondary = Color.White,
    onTertiary = Color.White,
    primaryContainer = Color(0xFFDDF3DF), onPrimaryContainer = CartioForest,
    secondaryContainer = Color(0xFFE7F3E5), onSecondaryContainer = CartioForest,
    surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFF3F7F1),
    surfaceContainer = Color(0xFFEDF3EB), surfaceVariant = Color(0xFFE7EEE5),
    outlineVariant = Color(0xFFCDD8CA),
)

@Composable
fun CartioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
