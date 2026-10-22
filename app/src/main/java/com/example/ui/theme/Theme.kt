package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val PlanetaryDarkColorScheme = darkColorScheme(
    primary = AstralGold,
    onPrimary = TextOnGold,
    primaryContainer = CosmicCardElevated,
    onPrimaryContainer = AstralGoldLight,
    secondary = StarlightPurple,
    onSecondary = CosmicVoid,
    secondaryContainer = CosmicCard,
    onSecondaryContainer = TextPrimary,
    tertiary = CelestialTeal,
    onTertiary = CosmicVoid,
    tertiaryContainer = CosmicCard,
    onTertiaryContainer = TextPrimary,
    background = CosmicVoid,
    onBackground = TextPrimary,
    surface = CosmicSurface,
    onSurface = TextPrimary,
    surfaceVariant = CosmicSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = CosmicBorder
)

@Composable
fun PlanetaryMagickTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PlanetaryDarkColorScheme,
        typography = AppTypography,
        content = content
    )
}
