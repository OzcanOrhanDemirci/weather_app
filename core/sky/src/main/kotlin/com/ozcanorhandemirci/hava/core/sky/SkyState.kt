package com.ozcanorhandemirci.hava.core.sky

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.ozcanorhandemirci.hava.core.designsystem.theme.SkyPalette

/** How much of the sky to draw. A card in a list cannot afford what a screen can. */
enum class SkyDetail {
    /** A full screen backdrop. */
    Full,

    /** A card sized backdrop, drawn many times at once. */
    Miniature,
}

/**
 * One sky, resolved.
 *
 * Everything here is derived from [SkyConditions] and nothing here changes
 * between frames, so it is computed once when the conditions change rather than
 * inside the drawing loop.
 */
@Immutable
data class SkyState(
    val conditions: SkyConditions,
    val palette: SkyPalette,
    val sun: SolarPosition,
    val moon: SolarPosition,
    val moonPhase: MoonPhase,
) {

    /**
     * How fully night has fallen, from 0 in daylight to 1 once the sun is far
     * enough down that the stars are out.
     *
     * A boolean would make the stars appear all at once. Dusk is the most
     * interesting part of a day and it deserves a number rather than a switch.
     */
    val nightfall: Float
        get() = ((-sun.altitudeDegrees - CIVIL_TWILIGHT) / (ASTRONOMICAL_TWILIGHT - CIVIL_TWILIGHT))
            .coerceIn(0.0, 1.0)
            .toFloat()

    /** Which body is up, and therefore which one to draw. */
    val visibleBody: SolarPosition
        get() = if (sun.altitudeDegrees > BODY_HANDOVER_ALTITUDE) sun else moon

    val showsSun: Boolean
        get() = sun.altitudeDegrees > BODY_HANDOVER_ALTITUDE

    private companion object {
        const val CIVIL_TWILIGHT = 0.0
        const val ASTRONOMICAL_TWILIGHT = 16.0

        /** The sun keeps the sky until it is properly under the horizon. */
        const val BODY_HANDOVER_ALTITUDE = -2.0
    }
}

@Composable
fun rememberSkyState(conditions: SkyConditions): SkyState = remember(conditions) {
    SkyState(
        conditions = conditions,
        palette = SkyPaletteFactory.create(conditions),
        sun = SolarGeometry.positionAt(
            instant = conditions.instant,
            latitude = conditions.coordinates.latitude,
            longitude = conditions.coordinates.longitude,
        ),
        moon = LunarGeometry.positionAt(
            instant = conditions.instant,
            latitude = conditions.coordinates.latitude,
            longitude = conditions.coordinates.longitude,
        ),
        moonPhase = LunarGeometry.phaseAt(conditions.instant),
    )
}

/**
 * Places a body on the screen.
 *
 * Height comes from its altitude above the horizon and side to side position
 * from its azimuth, so the arc a viewer sees is the arc the sky actually has:
 * high and long in summer, low and short in winter, and mirrored below the
 * equator, where midday is in the north.
 */
internal fun SolarPosition.toScreenPosition(latitude: Double, size: Size): Offset {
    val meridian = if (latitude >= 0.0) SOUTH else NORTH
    val offsetFromMeridian = (azimuthDegrees - meridian).mod(FULL_TURN)
        .let { if (it > HALF_TURN) it - FULL_TURN else it }

    val horizontal = 0.5 + offsetFromMeridian / VISIBLE_COMPASS_SPAN
    val vertical = HORIZON_HEIGHT -
        (HORIZON_HEIGHT - ZENITH_HEIGHT) * (altitudeDegrees / QUARTER_TURN).coerceIn(-0.25, 1.0)

    return Offset(x = (horizontal * size.width).toFloat(), y = (vertical * size.height).toFloat())
}

private const val FULL_TURN = 360.0
private const val HALF_TURN = 180.0
private const val QUARTER_TURN = 90.0
private const val SOUTH = 180.0
private const val NORTH = 0.0

/** How much of the compass the width of the screen stands for. */
private const val VISIBLE_COMPASS_SPAN = 220.0

private const val HORIZON_HEIGHT = 0.88
private const val ZENITH_HEIGHT = 0.07
