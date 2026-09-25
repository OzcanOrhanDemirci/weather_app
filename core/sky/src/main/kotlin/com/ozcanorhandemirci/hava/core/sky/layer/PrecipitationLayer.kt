package com.ozcanorhandemirci.hava.core.sky.layer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.ozcanorhandemirci.hava.core.designsystem.theme.SkyPalette
import com.ozcanorhandemirci.hava.core.model.Intensity
import com.ozcanorhandemirci.hava.core.model.Precipitation
import kotlin.math.sin
import kotlin.random.Random

/**
 * Falling particles.
 *
 * Position is a function of elapsed time rather than a value carried from the
 * previous frame. A dropped frame therefore skips ahead instead of falling
 * behind, and the field can be drawn from any moment, which is what lets the
 * hourly forecast be scrubbed and the rain follow.
 *
 * Particles are split into depth bands. The near band falls faster, is drawn
 * longer and brighter; the far band is slow and faint. Each band is one draw
 * call, so the whole of a heavy downpour costs three.
 */
internal class PrecipitationField(count: Int, seed: Long) {

    private val random = Random(seed)

    val bands: List<Band> = List(BAND_COUNT) { index ->
        val depth = (index + 1f) / BAND_COUNT
        val share = count / BAND_COUNT
        Band(
            depth = depth,
            startX = FloatArray(share) { random.nextFloat() },
            startY = FloatArray(share) { random.nextFloat() },
            speedScatter = FloatArray(share) { SPEED_SCATTER_FLOOR + random.nextFloat() * SPEED_SCATTER_RANGE },
            swayPhase = FloatArray(share) { random.nextFloat() * TWO_PI },
        )
    }

    class Band(
        val depth: Float,
        val startX: FloatArray,
        val startY: FloatArray,
        val speedScatter: FloatArray,
        val swayPhase: FloatArray,
    ) {
        val size: Int get() = startX.size

        /** Reused so that drawing does not allocate a list on every frame. */
        val points: MutableList<Offset> = MutableList(startX.size * 2) { Offset.Zero }
    }

    private companion object {
        const val BAND_COUNT = 3
        const val SPEED_SCATTER_FLOOR = 0.75f
        const val SPEED_SCATTER_RANGE = 0.5f
        const val TWO_PI = (2.0 * Math.PI).toFloat()
    }
}

internal fun DrawScope.drawPrecipitation(
    field: PrecipitationField,
    palette: SkyPalette,
    precipitation: Precipitation,
    intensity: Intensity,
    seconds: Float,
    windSpeedKph: Double,
    windDirectionDegrees: Int,
) {
    if (precipitation == Precipitation.NONE) return

    val strength = when (intensity) {
        Intensity.NONE -> return
        Intensity.LIGHT -> 0.42f
        Intensity.MODERATE -> 0.72f
        Intensity.HEAVY -> 1f
    }

    // Wind is reported as the direction it comes from, so the drift is opposite
    // to it. Only the across screen part of it is visible.
    val drift = (-sin(Math.toRadians(windDirectionDegrees.toDouble())) *
        (windSpeedKph / REFERENCE_WIND_KPH).coerceIn(0.0, 1.6)).toFloat()

    when (precipitation) {
        Precipitation.RAIN -> drawRain(field, palette, strength, seconds, drift)
        Precipitation.SLEET -> drawRain(field, palette, strength * 0.8f, seconds, drift)
        Precipitation.SNOW -> drawSnow(field, palette, strength, seconds, drift)
        Precipitation.NONE -> Unit
    }
}

private fun DrawScope.drawRain(
    field: PrecipitationField,
    palette: SkyPalette,
    strength: Float,
    seconds: Float,
    drift: Float,
) {
    field.bands.forEach { band ->
        val fallSpeed = RAIN_SLOWEST_FALL + band.depth * RAIN_FALL_RANGE
        val streakLength = (RAIN_SHORTEST + band.depth * RAIN_LENGTH_RANGE) * size.height
        val slant = drift * streakLength * RAIN_SLANT

        for (i in 0 until band.size) {
            val progress = (band.startY[i] + seconds * fallSpeed * band.speedScatter[i]).mod(1f)
            val x = (band.startX[i] + drift * progress * RAIN_HORIZONTAL_TRAVEL).mod(1f) * size.width
            val y = progress * size.height

            band.points[i * 2] = Offset(x, y)
            band.points[i * 2 + 1] = Offset(x + slant, y + streakLength)
        }

        drawPoints(
            points = band.points,
            pointMode = PointMode.Lines,
            color = rainColor(palette).copy(alpha = strength * band.depth * RAIN_OPACITY),
            strokeWidth = (RAIN_THINNEST + band.depth * RAIN_WIDTH_RANGE) * density,
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawSnow(
    field: PrecipitationField,
    palette: SkyPalette,
    strength: Float,
    seconds: Float,
    drift: Float,
) {
    field.bands.forEach { band ->
        val fallSpeed = SNOW_SLOWEST_FALL + band.depth * SNOW_FALL_RANGE

        for (i in 0 until band.size) {
            val progress = (band.startY[i] + seconds * fallSpeed * band.speedScatter[i]).mod(1f)
            // A flake does not fall straight. It swings, and the swing is what
            // separates snow from slow rain.
            val sway = sin(seconds * SNOW_SWAY_SPEED + band.swayPhase[i]) * SNOW_SWAY_WIDTH
            val x = (band.startX[i] + sway + drift * progress * SNOW_HORIZONTAL_TRAVEL).mod(1f) * size.width
            val y = progress * size.height

            band.points[i * 2] = Offset(x, y)
            band.points[i * 2 + 1] = Offset(x, y)
        }

        drawPoints(
            points = band.points,
            pointMode = PointMode.Points,
            color = palette.cloud.copy(alpha = strength * band.depth * SNOW_OPACITY),
            strokeWidth = (SNOW_SMALLEST + band.depth * SNOW_SIZE_RANGE) * density,
            cap = StrokeCap.Round,
        )
    }
}

/** Rain takes its color from the sky it falls through, lifted so it stays visible. */
private fun rainColor(palette: SkyPalette): Color =
    if (palette.isNight) palette.cloud else palette.haze

/** A wind at which drift reaches its reference amount. */
private const val REFERENCE_WIND_KPH = 45.0

private const val RAIN_SLOWEST_FALL = 0.55f
private const val RAIN_FALL_RANGE = 1.15f
private const val RAIN_SHORTEST = 0.018f
private const val RAIN_LENGTH_RANGE = 0.042f
private const val RAIN_SLANT = 0.85f
private const val RAIN_HORIZONTAL_TRAVEL = 0.22f
private const val RAIN_THINNEST = 0.8f
private const val RAIN_WIDTH_RANGE = 1.0f
private const val RAIN_OPACITY = 0.55f

private const val SNOW_SLOWEST_FALL = 0.06f
private const val SNOW_FALL_RANGE = 0.14f
private const val SNOW_SWAY_SPEED = 0.6f
private const val SNOW_SWAY_WIDTH = 0.035f
private const val SNOW_HORIZONTAL_TRAVEL = 0.35f
private const val SNOW_SMALLEST = 1.6f
private const val SNOW_SIZE_RANGE = 2.6f
private const val SNOW_OPACITY = 0.85f
