package com.ozcanorhandemirci.hava.core.designsystem.modifier

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import com.ozcanorhandemirci.hava.core.designsystem.theme.SkyPalette

/**
 * Paints the base of a sky: the vertical fall from the zenith through the haze
 * to the horizon.
 *
 * This is the layer everything else in a sky is drawn over. Colour is placed
 * with stops rather than spread evenly, because the light in a real sky changes
 * slowly overhead and quickly near the ground.
 */
fun Modifier.skyGradient(palette: SkyPalette): Modifier = drawBehind {
    drawRect(
        brush = Brush.verticalGradient(
            0f to palette.zenith,
            ZENITH_HOLD to palette.zenith,
            HAZE_POSITION to palette.haze,
            1f to palette.horizon,
        ),
    )
}

/** How far down the zenith colour holds before it starts to give way. */
private const val ZENITH_HOLD = 0.18f

/** Where the haze band sits, low enough to read as distance. */
private const val HAZE_POSITION = 0.78f
