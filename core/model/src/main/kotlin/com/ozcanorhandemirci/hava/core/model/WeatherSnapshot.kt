package com.ozcanorhandemirci.hava.core.model

import java.time.Instant

/**
 * Everything the application knows about one city at one point in time.
 *
 * A snapshot is the unit that is fetched, cached and rendered, which keeps the
 * screens from assembling a view out of several independently aged pieces.
 */
data class WeatherSnapshot(
    val city: City,
    val current: CurrentWeather,
    val hourly: List<HourlyPoint>,
    val daily: List<DailyPoint>,
    val retrievedAt: Instant,
) {

    /** The forecast from [from] onwards, which is what an hourly strip should show. */
    fun hourlyFrom(from: Instant, count: Int): List<HourlyPoint> =
        hourly.asSequence()
            .filter { !it.time.isBefore(from) }
            .take(count)
            .toList()

    val today: DailyPoint?
        get() = daily.firstOrNull()
}
