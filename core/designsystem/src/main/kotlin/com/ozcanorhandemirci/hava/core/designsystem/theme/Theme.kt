package com.ozcanorhandemirci.hava.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

/**
 * Wraps the application in the sky it is currently showing.
 *
 * Passing a different [palette] repaints every component that reads a color
 * from the theme, which is how moving from a clear afternoon in Izmir to a
 * snowy night in Erzurum changes the whole interface and not only the backdrop.
 */
@Composable
fun HavaTheme(
    palette: SkyPalette = SkyPalette.Placeholder,
    content: @Composable () -> Unit,
) {
    val colorScheme = remember(palette) { palette.toColorScheme() }

    CompositionLocalProvider(
        LocalSkyPalette provides palette,
        LocalHavaTypography provides HavaTypographyDefaults,
        LocalReducedMotion provides rememberSystemReducedMotion(),
        LocalContentColor provides palette.content,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = HavaMaterialTypography,
            shapes = HavaShapes,
            content = content,
        )
    }
}

/** Entry point for the tokens that Material does not model. */
object HavaTheme {

    val sky: SkyPalette
        @Composable @ReadOnlyComposable get() = LocalSkyPalette.current

    val typography: HavaTypography
        @Composable @ReadOnlyComposable get() = LocalHavaTypography.current

    val spacing: HavaSpacing get() = HavaSpacing
}

/**
 * Material components are used throughout, so they are given the sky rather
 * than left on a palette of their own that would drift away from it.
 */
private fun SkyPalette.toColorScheme(): ColorScheme {
    val base = if (isNight) darkColorScheme() else lightColorScheme()
    val onAccent = if (isNight) Color(0xFF0A0F1F) else Color(0xFF10233B)

    return base.copy(
        primary = accent,
        onPrimary = onAccent,
        secondary = luminary,
        onSecondary = onAccent,
        background = zenith,
        onBackground = content,
        surface = glass,
        onSurface = content,
        surfaceVariant = glass,
        onSurfaceVariant = contentMuted,
        outline = glassEdge,
        outlineVariant = glassEdge,
        scrim = Color.Black.copy(alpha = 0.5f),
    )
}
