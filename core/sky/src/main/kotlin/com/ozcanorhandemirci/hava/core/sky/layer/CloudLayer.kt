package com.ozcanorhandemirci.hava.core.sky.layer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.ozcanorhandemirci.hava.core.designsystem.theme.SkyPalette
import kotlin.math.abs
import kotlin.random.Random

/**
 * Clouds, as a fixed arrangement of soft shapes that drift.
 *
 * Each cloud is several overlapping puffs rather than one shape, because the
 * ragged outline is what makes a cloud read as a cloud. The puffs are laid out
 * along a line, biased upwards and tapered at the ends, so the result has the
 * flat base and piled top of the real thing.
 *
 * Clouds sit at two depths and drift at different speeds, so the sky has
 * parallax: the near band crosses the screen while the far one barely moves.
 */
internal class CloudField(count: Int, seed: Long) {

    private val random = Random(seed)

    val clouds: List<Cloud> = List(count) { index ->
        val depth = if (index % 2 == 0) NEAR else FAR
        Cloud(
            offset = Offset(random.nextFloat(), TOP_OF_BAND + random.nextFloat() * BAND_HEIGHT),
            scale = MINIMUM_SCALE + random.nextFloat() * (MAXIMUM_SCALE - MINIMUM_SCALE),
            depth = depth,
            speed = (SLOWEST_DRIFT + random.nextFloat() * (FASTEST_DRIFT - SLOWEST_DRIFT)) * depth,
            puffs = List(PUFFS_PER_CLOUD) { puff ->
                // Puffs are laid out along a line and biased upwards, because a
                // cloud is flat underneath and piled on top. Spreading them
                // evenly in both directions produces a bunch of bubbles
                // instead.
                val alongCloud = (puff.toFloat() / (PUFFS_PER_CLOUD - 1) - 0.5f) * PUFF_SPREAD
                val rise = random.nextFloat()
                Puff(
                    offset = Offset(
                        x = alongCloud + (random.nextFloat() - 0.5f) * PUFF_JITTER,
                        y = -rise * rise * PUFF_RISE,
                    ),
                    // The middle of a cloud towers; the ends taper.
                    radius = (SMALLEST_PUFF + random.nextFloat() * (LARGEST_PUFF - SMALLEST_PUFF)) *
                        (1f - TAPER * abs(alongCloud) / (PUFF_SPREAD * 0.5f)),
                )
            },
        )
    }

    class Cloud(
        val offset: Offset,
        val scale: Float,
        val depth: Float,
        val speed: Float,
        val puffs: List<Puff>,
    )

    class Puff(val offset: Offset, val radius: Float)

    private companion object {
        const val NEAR = 1f
        const val FAR = 0.45f
        const val TOP_OF_BAND = 0.04f
        const val BAND_HEIGHT = 0.5f
        const val MINIMUM_SCALE = 0.7f
        const val MAXIMUM_SCALE = 1.5f
        const val SLOWEST_DRIFT = 0.004f
        const val FASTEST_DRIFT = 0.016f
        const val PUFFS_PER_CLOUD = 7
        const val PUFF_SPREAD = 0.34f
        const val PUFF_JITTER = 0.045f
        const val PUFF_RISE = 0.075f
        const val TAPER = 0.55f
        const val SMALLEST_PUFF = 0.055f
        const val LARGEST_PUFF = 0.105f
    }
}

internal fun DrawScope.drawClouds(
    field: CloudField,
    palette: SkyPalette,
    coverage: Float,
    seconds: Float,
    windFactor: Float,
) {
    if (coverage <= 0.01f) return

    val visible = (field.clouds.size * coverage).toInt().coerceAtLeast(1)

    field.clouds.take(visible).forEach { cloud ->
        // Drift wraps through one and a half widths so a cloud leaves the screen
        // completely before it returns on the other side.
        val drift = (cloud.offset.x + seconds * cloud.speed * windFactor).mod(WRAP_WIDTH) - WRAP_MARGIN
        val center = Offset(drift * size.width, cloud.offset.y * size.height)
        val scale = cloud.scale * size.minDimension

        // The shaded underside of the whole cloud is laid down before any of the
        // lit body, so the shadow does not show through the puff in front of it.
        cloud.puffs.forEach { puff ->
            drawSoftPuff(
                center = puffCenter(center, puff, scale).let { it.copy(y = it.y + puff.radius * scale * SHADE_DROP) },
                radiusX = puff.radius * scale,
                color = palette.cloudShade,
                alpha = coverage * cloud.depth * SHADE_STRENGTH,
            )
        }
        cloud.puffs.forEach { puff ->
            drawSoftPuff(
                center = puffCenter(center, puff, scale),
                radiusX = puff.radius * scale,
                color = palette.cloud,
                alpha = coverage * cloud.depth,
            )
        }
    }
}

private fun puffCenter(cloudCenter: Offset, puff: CloudField.Puff, scale: Float) = Offset(
    x = cloudCenter.x + puff.offset.x * scale,
    y = cloudCenter.y + puff.offset.y * scale,
)

/**
 * A puff with a soft edge, built from concentric ovals rather than a gradient
 * brush, so drawing a sky allocates nothing.
 *
 * Ovals rather than circles: a cloud is far wider than it is tall, and circles
 * of any arrangement read as bubbles. Rings are faint individually and only
 * become solid where several puffs overlap, which is where a cloud is thickest.
 */
private fun DrawScope.drawSoftPuff(
    center: Offset,
    radiusX: Float,
    color: Color,
    alpha: Float,
) {
    if (alpha <= 0.01f) return
    val radiusY = radiusX * FLATTENING

    RINGS.forEach { (scale, strength) ->
        drawOval(
            color = color.copy(alpha = alpha * strength),
            topLeft = Offset(center.x - radiusX * scale, center.y - radiusY * scale),
            size = Size(radiusX * 2f * scale, radiusY * 2f * scale),
        )
    }
}

/** Ring radius as a share of the puff, paired with its share of the opacity. */
private val RINGS = listOf(
    1.00f to 0.16f,
    0.82f to 0.18f,
    0.63f to 0.20f,
    0.42f to 0.24f,
)

/** How much flatter a cloud is than it is wide. */
private const val FLATTENING = 0.52f

private const val WRAP_WIDTH = 1.5f
private const val WRAP_MARGIN = 0.25f
private const val SHADE_DROP = 0.30f
private const val SHADE_STRENGTH = 0.65f
