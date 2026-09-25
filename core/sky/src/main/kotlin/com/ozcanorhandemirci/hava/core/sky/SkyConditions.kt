package com.ozcanorhandemirci.hava.core.sky

import androidx.compose.runtime.Immutable
import com.ozcanorhandemirci.hava.core.model.Coordinates
import com.ozcanorhandemirci.hava.core.model.WeatherKind
import java.time.Instant

/**
 * Everything the backdrop needs to draw one sky.
 *
 * The place and the moment are carried rather than a precomputed sun position,
 * because the same city is scrubbed through the hours of its forecast and the
 * sky has to follow.
 */
@Immutable
data class SkyConditions(
    val kind: WeatherKind,
    val instant: Instant,
    val coordinates: Coordinates,
    val windSpeedKph: Double,
    val windDirectionDegrees: Int,
) {
    companion object {
        /** A clear afternoon, used by previews and before a forecast arrives. */
        fun preview(
            kind: WeatherKind,
            instant: Instant = Instant.parse("2026-06-21T12:00:00Z"),
            coordinates: Coordinates = Coordinates(38.4237, 27.1428),
        ) = SkyConditions(
            kind = kind,
            instant = instant,
            coordinates = coordinates,
            windSpeedKph = 12.0,
            windDirectionDegrees = 225,
        )
    }
}
