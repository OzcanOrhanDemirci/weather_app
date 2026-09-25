package com.ozcanorhandemirci.hava.feature.cities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ozcanorhandemirci.hava.core.data.CityRepository
import com.ozcanorhandemirci.hava.core.data.WeatherRepository
import com.ozcanorhandemirci.hava.core.model.Outcome
import com.ozcanorhandemirci.hava.core.model.WeatherError
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CitiesViewModel @Inject constructor(
    private val cityRepository: CityRepository,
    private val weatherRepository: WeatherRepository,
) : ViewModel() {

    private val isRefreshing = MutableStateFlow(false)
    private val problem = MutableStateFlow<WeatherError?>(null)

    val uiState: StateFlow<CitiesUiState> = combine(
        cityRepository.observeCities(),
        cityRepository.observeFavoriteIds(),
        weatherRepository.observeForecasts(),
        isRefreshing,
        problem,
    ) { cities, favorites, forecasts, refreshing, failure ->
        when {
            cities.isEmpty() && failure != null -> CitiesUiState.Failed(failure)
            cities.isEmpty() -> CitiesUiState.Empty
            else -> CitiesUiState.Content(
                cities = cities.map { city ->
                    CityWeather(
                        city = city,
                        forecast = forecasts[city.id],
                        isFavorite = city.id in favorites,
                    )
                },
                isRefreshing = refreshing,
                problem = failure,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        // Kept alive briefly across a rotation, so returning to the screen does
        // not read the database and parse every stored forecast again.
        started = SharingStarted.WhileSubscribed(SUBSCRIPTION_GRACE_MILLIS),
        initialValue = CitiesUiState.Loading,
    )

    init {
        refresh()
    }

    fun refresh() {
        if (isRefreshing.value) return

        viewModelScope.launch {
            isRefreshing.value = true

            // One reading of the current list is enough to know what to ask
            // for; the screen keeps observing it separately.
            val cities = cityRepository.observeCities().first()

            problem.value = when (val outcome = weatherRepository.refreshStale(cities)) {
                is Outcome.Success -> null
                is Outcome.Failure -> outcome.reason
            }
            isRefreshing.value = false
        }
    }

    fun toggleFavorite(cityId: Long, isFavorite: Boolean) {
        viewModelScope.launch { cityRepository.setFavorite(cityId, isFavorite) }
    }

    private companion object {
        const val SUBSCRIPTION_GRACE_MILLIS = 5_000L
    }
}
