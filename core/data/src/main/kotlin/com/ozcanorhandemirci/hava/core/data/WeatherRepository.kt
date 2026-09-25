package com.ozcanorhandemirci.hava.core.data

import com.ozcanorhandemirci.hava.core.data.model.CachedForecast
import com.ozcanorhandemirci.hava.core.model.City
import com.ozcanorhandemirci.hava.core.model.Outcome
import kotlinx.coroutines.flow.Flow

/**
 * Forecasts, read from the device and refreshed from the service.
 *
 * Reading and refreshing are separate on purpose. What is observed is whatever
 * the device holds, so a screen has something to show the instant it opens and
 * keeps showing it when the network is gone. Refreshing is an action with a
 * result, so a failure can be reported next to content rather than instead of
 * it.
 */
interface WeatherRepository {

    fun observeForecast(cityId: Long): Flow<CachedForecast?>

    fun observeForecasts(): Flow<Map<Long, CachedForecast>>

    suspend fun refresh(city: City): Outcome<Unit>

    /**
     * Fetches again for the given cities.
     *
     * With [force] left alone, only the cities whose stored forecast is missing
     * or old are asked for, because the service recomputes a few times an hour
     * and asking more often spends the battery of the device to receive the
     * same numbers. A reader who pulls the list down is not making that
     * calculation, they are asking, so that path sets [force].
     *
     * Reports a failure only when nothing could be refreshed. If some cities
     * answered, the screen has new weather to show and an error would be
     * misleading.
     */
    suspend fun refresh(cities: List<City>, force: Boolean = false): Outcome<Unit>
}
