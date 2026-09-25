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
     * Refreshes the cities whose stored forecast is missing or old.
     *
     * Reports a failure only when nothing could be refreshed. If some cities
     * answered, the screen has new weather to show and an error would be
     * misleading.
     */
    suspend fun refreshStale(cities: List<City>): Outcome<Unit>
}
