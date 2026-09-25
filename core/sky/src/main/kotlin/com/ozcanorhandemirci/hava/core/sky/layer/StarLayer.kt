package com.ozcanorhandemirci.hava.core.sky.layer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.ozcanorhandemirci.hava.core.designsystem.theme.SkyPalette
import kotlin.math.sin
import kotlin.random.Random

/**
 * A fixed field of stars.
 *
 * Positions are drawn once from a seeded generator, so the same city shows the
 * same sky every time it is opened rather than a new arrangement on each
 * recomposition.
 *
 * Stars are grouped into bands of brightness. Each band is drawn in a single
 * call, which turns a few hundred draw operations per frame into eight. Within
 * a band the stars share a twinkle, given a different phase per band, which is
 * indistinguishable from individual twinkling at this size and costs nothing.
 */
internal class StarField(count: Int, seed: Long) {

    private val random = Random(seed)

    val bands: List<Band> = List(BAND_COUNT) { band ->
        val share = count / BAND_COUNT
        Band(
            points = List(share) {
                Offset(
                    x = random.nextFloat(),
                    // Stars thin out towards the horizon, where the atmosphere
                    // is thickest and a city glows.
                    y = random.nextFloat() * random.nextFloat() * HIGHEST_STAR_BELOW_HORIZON,
                )
            },
            brightness = MINIMUM_BRIGHTNESS +
                (band.toFloat() / BAND_COUNT) * (1f - MINIMUM_BRIGHTNESS),
            radius = SMALLEST_STAR + (band.toFloat() / BAND_COUNT) * (LARGEST_STAR - SMALLEST_STAR),
            twinklePhase = random.nextFloat() * TWO_PI,
            twinkleSpeed = TWINKLE_SLOWEST + random.nextFloat() * (TWINKLE_FASTEST - TWINKLE_SLOWEST),
        )
    }

    class Band(
        val points: List<Offset>,
        val brightness: Float,
        val radius: Float,
        val twinklePhase: Float,
        val twinkleSpeed: Float,
    ) {
        /** Reused between frames so drawing a star field allocates nothing. */
        val scaled: MutableList<Offset> = points.toMutableList()
    }

    private companion object {
        const val BAND_COUNT = 8
        const val MINIMUM_BRIGHTNESS = 0.25f
        const val SMALLEST_STAR = 0.7f
        const val LARGEST_STAR = 1.9f
        const val HIGHEST_STAR_BELOW_HORIZON = 0.86f
        const val TWINKLE_SLOWEST = 0.5f
        const val TWINKLE_FASTEST = 1.8f
        const val TWO_PI = (2.0 * Math.PI).toFloat()
    }
}

/**
 * Draws the stars, faded by how far night has progressed and by how much cloud
 * stands between the ground and them.
 */
internal fun DrawScope.drawStars(
    field: StarField,
    palette: SkyPalette,
    nightfall: Float,
    obscured: Float,
    seconds: Float,
) {
    val visibility = nightfall * (1f - obscured)
    if (visibility <= 0f) return

    for (band in field.bands) {
        val twinkle = 0.75f + 0.25f * sin(seconds * band.twinkleSpeed + band.twinklePhase)
        val alpha = band.brightness * visibility * twinkle
        if (alpha <= 0.01f) continue

        band.points.forEachIndexed { index, point ->
            band.scaled[index] = Offset(point.x * size.width, point.y * size.height)
        }

        drawPoints(
            points = band.scaled,
            pointMode = PointMode.Points,
            // Multiplied by the screen density so a star is the same size to the
            // eye on every device rather than the same number of pixels.
            strokeWidth = band.radius * density,
            color = palette.luminary.copy(alpha = alpha),
            cap = StrokeCap.Round,
        )
    }
}
