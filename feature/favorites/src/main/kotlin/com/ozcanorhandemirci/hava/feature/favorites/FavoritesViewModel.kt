package com.ozcanorhandemirci.hava.feature.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ozcanorhandemirci.hava.core.data.CityRepository
import com.ozcanorhandemirci.hava.core.data.WeatherRepository
import com.ozcanorhandemirci.hava.core.ui.CitySummary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState
    data object Empty : FavoritesUiState
    data class Content(val cities: List<CitySummary>) : FavoritesUiState
}

/**
 * The kept places.
 *
 * There is no refreshing here and no error state. Favourites are a view of what
 * the device already holds, kept in step with the list by the same repository,
 * so a city favoured on one screen is favoured on the other before the finger
 * has left the glass.
 */
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    cityRepository: CityRepository,
    weatherRepository: WeatherRepository,
) : ViewModel() {

    val uiState: StateFlow<FavoritesUiState> = combine(
        cityRepository.observeFavorites(),
        weatherRepository.observeForecasts(),
    ) { favorites, forecasts ->
        if (favorites.isEmpty()) {
            FavoritesUiState.Empty
        } else {
            FavoritesUiState.Content(
                cities = favorites.map { city ->
                    CitySummary(
                        city = city,
                        snapshot = forecasts[city.id]?.snapshot,
                        isFavorite = true,
                    )
                },
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(SUBSCRIPTION_GRACE_MILLIS),
        initialValue = FavoritesUiState.Loading,
    )

    private companion object {
        const val SUBSCRIPTION_GRACE_MILLIS = 5_000L
    }
}
