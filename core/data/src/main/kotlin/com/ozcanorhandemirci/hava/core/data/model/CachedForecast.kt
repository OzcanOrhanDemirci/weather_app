package com.ozcanorhandemirci.hava.core.data.model

import com.ozcanorhandemirci.hava.core.model.WeatherSnapshot
import java.time.Duration
import java.time.Instant

/**
 * A forecast that is on the device, together with when it arrived.
 *
 * The age is carried alongside the forecast rather than hidden, because the
 * interface has something to say about it: a reading from four minutes ago is
 * shown without comment, while one from yesterday is shown with the fact that
 * it is from yesterday.
 */
data class CachedForecast(
    val snapshot: WeatherSnapshot,
    val retrievedAt: Instant,
) {

    fun age(now: Instant): Duration = Duration.between(retrievedAt, now)

    fun isStale(now: Instant): Boolean = age(now) > MAX_AGE

    companion object {
        /**
         * Open-Meteo recomputes roughly four times an hour, so asking more often
         * than this returns the same numbers and spends the battery of the
         * device to do it.
         */
        val MAX_AGE: Duration = Duration.ofMinutes(15)
    }
}
