package com.ozcanorhandemirci.hava.core.sky.layer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.ozcanorhandemirci.hava.core.designsystem.theme.SkyPalette
import kotlin.math.sin

/**
 * The base of every sky: the fall of colour from the zenith to the horizon.
 *
 * Stops are placed rather than spread evenly, because light overhead changes
 * slowly and light near the ground changes quickly.
 */
internal fun DrawScope.drawSkyGradient(palette: SkyPalette) {
    drawRect(
        brush = Brush.verticalGradient(
            0f to palette.zenith,
            ZENITH_HOLD to palette.zenith,
            HAZE_POSITION to palette.haze,
            1f to palette.horizon,
        ),
    )
}

/**
 * Fog, as slow horizontal banks with a wash over everything.
 *
 * Fog is drawn last of the weather layers because it stands between the viewer
 * and everything else, including the rain falling through it.
 */
internal fun DrawScope.drawFog(palette: SkyPalette, amount: Float, seconds: Float) {
    if (amount <= 0.01f) return

    drawRect(color = palette.haze.copy(alpha = amount * WASH_STRENGTH))

    repeat(BANK_COUNT) { index ->
        val phase = index.toFloat() / BANK_COUNT
        val drift = sin(seconds * BANK_DRIFT_SPEED + phase * TWO_PI) * BANK_TRAVEL
        val centre = (BANK_TOP + phase * BANK_SPREAD + drift).coerceIn(0f, 1f)

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    palette.cloud.copy(alpha = amount * BANK_STRENGTH),
                    Color.Transparent,
                ),
                startY = (centre - BANK_HEIGHT) * size.height,
                endY = (centre + BANK_HEIGHT) * size.height,
            ),
        )
    }
}

/**
 * The flash of a storm.
 *
 * Returns how brightly the sky is lit at this moment, on a schedule that
 * repeats but does not look regular: a strike is two flashes of unequal
 * strength a fraction of a second apart, then a long wait.
 */
internal fun lightningAt(seconds: Float): Float {
    val phase = seconds.mod(STRIKE_INTERVAL)

    return when {
        phase < FIRST_FLASH_END -> 1f - phase / FIRST_FLASH_END
        phase < SECOND_FLASH_START -> 0f
        phase < SECOND_FLASH_END ->
            (1f - (phase - SECOND_FLASH_START) / (SECOND_FLASH_END - SECOND_FLASH_START)) * SECOND_FLASH_STRENGTH

        else -> 0f
    }
}

internal fun DrawScope.drawLightning(palette: SkyPalette, brightness: Float) {
    if (brightness <= 0.01f) return

    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = brightness * FLASH_CORE),
                palette.luminary.copy(alpha = brightness * FLASH_SPREAD),
                Color.Transparent,
            ),
            center = Offset(size.width * FLASH_ORIGIN_X, size.height * FLASH_ORIGIN_Y),
            radius = size.maxDimension * FLASH_RADIUS,
        ),
    )
}

private const val ZENITH_HOLD = 0.18f
private const val HAZE_POSITION = 0.78f

private const val WASH_STRENGTH = 0.45f
private const val BANK_COUNT = 4
private const val BANK_TOP = 0.35f
private const val BANK_SPREAD = 0.5f
private const val BANK_HEIGHT = 0.12f
private const val BANK_DRIFT_SPEED = 0.08f
private const val BANK_TRAVEL = 0.05f
private const val BANK_STRENGTH = 0.32f
private const val TWO_PI = (2.0 * Math.PI).toFloat()

private const val STRIKE_INTERVAL = 7.4f
private const val FIRST_FLASH_END = 0.16f
private const val SECOND_FLASH_START = 0.26f
private const val SECOND_FLASH_END = 0.44f
private const val SECOND_FLASH_STRENGTH = 0.65f

private const val FLASH_CORE = 0.55f
private const val FLASH_SPREAD = 0.22f
private const val FLASH_ORIGIN_X = 0.68f
private const val FLASH_ORIGIN_Y = 0.22f
private const val FLASH_RADIUS = 0.9f
