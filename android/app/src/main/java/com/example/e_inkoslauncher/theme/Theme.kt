package com.example.e_inkoslauncher.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// =============================================================================
// Nothing OS Monochrome Theme
// Single dark scheme – pure grayscale palette
// =============================================================================

private val NothingDarkColorScheme = darkColorScheme(
    background        = Black,
    surface           = SurfaceDark,
    surfaceVariant    = SurfaceElevated,
    onBackground      = OnDarkPrimary,
    onSurface         = OnDarkPrimary,
    onSurfaceVariant  = OnDarkSecondary,
    primary           = SurfaceLight,
    onPrimary         = OnLightPrimary,
    secondary         = SurfaceLight,
    onSecondary       = OnLightPrimary,
    outline           = OnDarkTertiary,
    outlineVariant    = SurfaceElevated,
    error             = OnDarkSecondary,
)

@Composable
fun NothingLauncherTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NothingDarkColorScheme,
        typography  = NothingTypography,
        content     = content
    )
}

// Design Token constants (mirrors tokens.json / tokens.css)
object Tokens {
    // Spacing (dp)
    val ScreenMargin  = 20
    val CardGutter    = 16
    val CardPaddingSm = 14
    val CardPaddingMd = 18
    val CardPaddingLg = 20

    // Radii (dp)
    val RadiusCard   = 28
    val RadiusCardSm = 20
    val RadiusDock   = 9999
    val RadiusBar    = 4

    // Sizes (dp)
    val DockButtonSize  = 64
    val DockIconSize    = 28
    val StatusBarHeight = 48
    val DotSize         = 5
    val BarHeight       = 5
}
