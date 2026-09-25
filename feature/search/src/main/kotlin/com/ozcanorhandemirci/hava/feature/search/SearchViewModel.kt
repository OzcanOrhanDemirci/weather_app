package com.ozcanorhandemirci.hava.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ozcanorhandemirci.hava.core.data.CityRepository
import com.ozcanorhandemirci.hava.core.model.City
import com.ozcanorhandemirci.hava.core.model.Outcome
import com.ozcanorhandemirci.hava.core.model.WeatherError
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SearchUiState {

    /** Nothing has been typed, or not enough of it. */
    data object Idle : SearchUiState

    data object Searching : SearchUiState

    /** The service knows no place by that name. */
    data object NoMatches : SearchUiState

    data class Failed(val reason: WeatherError) : SearchUiState

    data class Results(val places: List<City>) : SearchUiState
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val cityRepository: CityRepository,
) : ViewModel() {

    private val typed = MutableStateFlow("")

    val query: StateFlow<String> = typed.asStateFlow()

    /**
     * Results for what has been typed.
     *
     * The query is settled before it is sent, because a search per keystroke
     * asks the service six times for one word and answers out of order.
     * Switching to the newest query rather than waiting for the previous one
     * means a slow answer to an abandoned search can never overwrite a fast
     * answer to the current one.
     */
    val uiState: StateFlow<SearchUiState> = typed
        .debounce { text -> if (text.isBlank()) 0L else SETTLE_MILLIS }
        .flatMapLatest { text ->
            flow {
                if (text.trim().length < MINIMUM_QUERY) {
                    emit(SearchUiState.Idle)
                    return@flow
                }

                emit(SearchUiState.Searching)
                emit(
                    when (val outcome = cityRepository.search(text)) {
                        is Outcome.Failure -> SearchUiState.Failed(outcome.reason)
                        is Outcome.Success ->
                            if (outcome.value.isEmpty()) {
                                SearchUiState.NoMatches
                            } else {
                                SearchUiState.Results(outcome.value)
                            }
                    },
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_GRACE_MILLIS),
            initialValue = SearchUiState.Idle,
        )

    fun onQueryChange(text: String) {
        typed.value = text
    }

    fun onClear() {
        typed.value = ""
    }

    /**
     * Stores a place so that it can be opened, cached and kept.
     *
     * Search results are not written to the device as they arrive. Typing three
     * letters would otherwise leave a dozen places behind that nobody asked
     * for.
     */
    fun remember(city: City, onStored: (Long) -> Unit) {
        viewModelScope.launch {
            cityRepository.remember(city)
            onStored(city.id)
        }
    }

    private companion object {
        const val SETTLE_MILLIS = 300L
        const val MINIMUM_QUERY = 2
        const val SUBSCRIPTION_GRACE_MILLIS = 5_000L
    }
}
