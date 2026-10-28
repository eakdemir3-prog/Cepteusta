package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ForestGreenDarkPrimary,
    onPrimary = ForestGreenDarkOnPrimary,
    primaryContainer = ForestGreenDarkContainer,
    onPrimaryContainer = ForestGreenDarkOnContainer,
    secondary = MintDarkSecondary,
    onSecondary = MintDarkSecondaryOn,
    secondaryContainer = MintDarkSecondaryContainer,
    onSecondaryContainer = MintDarkSecondaryOnContainer,
    tertiary = AccentLimeDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = OutlineDark,
    error = StatusError,
    errorContainer = StatusErrorContainer
)

private val LightColorScheme = lightColorScheme(
    primary = ForestGreenPrimary,
    onPrimary = ForestGreenOnPrimary,
    primaryContainer = ForestGreenContainer,
    onPrimaryContainer = ForestGreenOnContainer,
    secondary = MintSecondary,
    onSecondary = MintSecondaryOn,
    secondaryContainer = MintSecondaryContainer,
    onSecondaryContainer = MintSecondaryOnContainer,
    tertiary = AccentLime,
    background = WarmBackgroundLight,
    surface = WarmSurfaceLight,
    surfaceVariant = WarmSurfaceVariantLight,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = OutlineLight,
    error = StatusError,
    errorContainer = StatusErrorContainer
)

@Composable
fun CepteUstaTheme(
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
