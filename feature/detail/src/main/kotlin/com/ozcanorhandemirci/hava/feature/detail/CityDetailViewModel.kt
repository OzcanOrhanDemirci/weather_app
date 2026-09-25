package com.ozcanorhandemirci.hava.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
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
class CityDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cityRepository: CityRepository,
    private val weatherRepository: WeatherRepository,
) : ViewModel() {

    private val cityId: Long = savedStateHandle.toRoute<CityDetailDestination>().cityId

    private val isRefreshing = MutableStateFlow(false)
    private val problem = MutableStateFlow<WeatherError?>(null)

    val uiState: StateFlow<CityDetailUiState> = combine(
        cityRepository.observeCity(cityId),
        cityRepository.observeFavoriteIds(),
        weatherRepository.observeForecast(cityId),
        isRefreshing,
        problem,
    ) { city, favorites, forecast, refreshing, failure ->
        when {
            city == null -> CityDetailUiState.Unknown
            forecast == null && failure != null -> CityDetailUiState.Failed(city, failure)
            forecast == null -> CityDetailUiState.Loading
            else -> CityDetailUiState.Content(
                city = city,
                forecast = forecast,
                isFavorite = cityId in favorites,
                isRefreshing = refreshing,
                problem = failure,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(SUBSCRIPTION_GRACE_MILLIS),
        initialValue = CityDetailUiState.Loading,
    )

    init {
        refresh()
    }

    fun refresh() {
        if (isRefreshing.value) return

        viewModelScope.launch {
            isRefreshing.value = true
            val city = cityRepository.observeCity(cityId).first()

            problem.value = when {
                city == null -> WeatherError.UnknownPlace
                else -> when (val outcome = weatherRepository.refresh(city)) {
                    is Outcome.Success -> null
                    is Outcome.Failure -> outcome.reason
                }
            }
            isRefreshing.value = false
        }
    }

    fun setFavorite(isFavorite: Boolean) {
        viewModelScope.launch { cityRepository.setFavorite(cityId, isFavorite) }
    }

    private companion object {
        const val SUBSCRIPTION_GRACE_MILLIS = 5_000L
    }
}
