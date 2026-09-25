package com.ozcanorhandemirci.hava.core.sky.layer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.ozcanorhandemirci.hava.core.designsystem.theme.SkyPalette
import com.ozcanorhandemirci.hava.core.sky.MoonPhase
import kotlin.math.abs

/**
 * Draws the sun: a disc with the glow the atmosphere puts around it.
 *
 * The glow is larger and warmer near the horizon, where the light travels
 * through more air, which is the whole reason a sunset looks different from a
 * midday sun rather than merely lower.
 */
internal fun DrawScope.drawSun(
    center: Offset,
    palette: SkyPalette,
    altitudeDegrees: Double,
    obscured: Float,
) {
    val clarity = (1f - obscured).coerceIn(0f, 1f)
    if (clarity <= 0.02f) return

    val horizonProximity = (1.0 - (altitudeDegrees / HORIZON_GLOW_RANGE).coerceIn(0.0, 1.0)).toFloat()
    val radius = size.minDimension * SUN_RADIUS
    val glowRadius = radius * (GLOW_AT_ZENITH + horizonProximity * GLOW_GROWTH_AT_HORIZON)

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                palette.luminaryGlow.copy(alpha = palette.luminaryGlow.alpha * clarity),
                Color.Transparent,
            ),
            center = center,
            radius = glowRadius,
        ),
        radius = glowRadius,
        center = center,
    )

    drawCircle(
        color = palette.luminary.copy(alpha = clarity),
        radius = radius,
        center = center,
    )
}

/**
 * The glow of the sun or the moon without the body itself.
 *
 * A backdrop keeps the warmth the light throws across the sky and drops the
 * disc, which would otherwise appear between two cards as a bright fragment
 * that reads as a defect rather than as the sun.
 */
internal fun DrawScope.drawLuminaryGlow(center: Offset, palette: SkyPalette, obscured: Float) {
    val clarity = (1f - obscured).coerceIn(0f, 1f)
    if (clarity <= 0.02f) return

    val radius = size.minDimension * SUN_RADIUS * BACKDROP_GLOW_SPREAD

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                palette.luminaryGlow.copy(alpha = palette.luminaryGlow.alpha * clarity),
                Color.Transparent,
            ),
            center = center,
            radius = radius,
        ),
        radius = radius,
        center = center,
    )
}

/**
 * Draws the moon with the phase it actually has tonight.
 *
 * The lit shape is made by covering a bright disc with a shadow disc offset
 * sideways. Sliding the shadow across produces the whole sequence, from a thin
 * crescent through the quarters to full, without a separate drawing for each.
 */
internal fun DrawScope.drawMoon(
    center: Offset,
    palette: SkyPalette,
    phase: MoonPhase,
    obscured: Float,
) {
    val clarity = (1f - obscured).coerceIn(0f, 1f)
    if (clarity <= 0.02f || phase.isNew) return

    val radius = size.minDimension * MOON_RADIUS

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                palette.luminaryGlow.copy(alpha = palette.luminaryGlow.alpha * clarity * MOON_GLOW_STRENGTH),
                Color.Transparent,
            ),
            center = center,
            radius = radius * MOON_GLOW_SPREAD,
        ),
        radius = radius * MOON_GLOW_SPREAD,
        center = center,
    )

    // The disc and its shadow are drawn into a separate layer so the shadow can
    // erase the disc without also erasing the sky behind it.
    drawContext.canvas.saveLayer(
        bounds = androidx.compose.ui.geometry.Rect(
            left = center.x - radius * MOON_GLOW_SPREAD,
            top = center.y - radius * MOON_GLOW_SPREAD,
            right = center.x + radius * MOON_GLOW_SPREAD,
            bottom = center.y + radius * MOON_GLOW_SPREAD,
        ),
        paint = androidx.compose.ui.graphics.Paint(),
    )

    drawCircle(
        color = palette.luminary.copy(alpha = clarity),
        radius = radius,
        center = center,
    )

    if (!phase.isFull) {
        // How far the shadow sits from the center. At the quarters it sits on
        // the center and cuts the disc in half; near new moon it barely
        // overlaps, leaving a sliver.
        val shadowOffset = radius * 2f * (0.5f - abs(phase.illumination.toFloat() - 0.5f)).let {
            1f - 2f * it
        }
        val direction = if (phase.isWaxing) -1f else 1f

        drawCircle(
            color = Color.Transparent,
            radius = radius * SHADOW_RADIUS,
            center = Offset(center.x + direction * shadowOffset, center.y),
            blendMode = BlendMode.Clear,
        )
    }

    drawContext.canvas.restore()
}

/** Radius of the sun as a share of the smaller screen dimension. */
private const val SUN_RADIUS = 0.072f
private const val MOON_RADIUS = 0.055f

private const val GLOW_AT_ZENITH = 2.2f
private const val GLOW_GROWTH_AT_HORIZON = 3.4f

/** Above this height the atmosphere no longer widens the glow. */
private const val HORIZON_GLOW_RANGE = 25.0

/** The backdrop glow is wide and soft, because nothing marks its center. */
private const val BACKDROP_GLOW_SPREAD = 5.5f

private const val MOON_GLOW_STRENGTH = 0.7f
private const val MOON_GLOW_SPREAD = 3.2f

/**
 * The shadow is slightly larger than the moon so its edge stays a clean arc
 * instead of meeting the rim at a visible corner.
 */
private const val SHADOW_RADIUS = 1.02f
