package fi.cartio.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CartioLightGreen, secondary = CartioGreen, tertiary = CartioForest,
    background = Color(0xFF101713), surface = Color(0xFF17201A), onSurface = Color(0xFFF2F4F2)
)

private val LightColorScheme = lightColorScheme(
    primary = CartioGreen, secondary = CartioForest, tertiary = CartioLightGreen,
    background = CartioCream, surface = Color.White, onPrimary = Color.White,
    onBackground = CartioInk, onSurface = CartioInk,
    onSecondary = Color.White,
    onTertiary = Color.White,
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
