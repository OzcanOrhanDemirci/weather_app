package com.ozcanorhandemirci.hava.feature.detail

import com.ozcanorhandemirci.hava.core.data.model.CachedForecast
import com.ozcanorhandemirci.hava.core.model.City
import com.ozcanorhandemirci.hava.core.model.WeatherError

sealed interface CityDetailUiState {

    data object Loading : CityDetailUiState

    /** The place is not on the device, which happens if it is opened from a stale link. */
    data object Unknown : CityDetailUiState

    /** Nothing is stored for this city and nothing could be fetched. */
    data class Failed(val city: City?, val reason: WeatherError) : CityDetailUiState

    data class Content(
        val city: City,
        val forecast: CachedForecast,
        val isFavorite: Boolean,
        val isRefreshing: Boolean,
        val problem: WeatherError?,
    ) : CityDetailUiState
}
