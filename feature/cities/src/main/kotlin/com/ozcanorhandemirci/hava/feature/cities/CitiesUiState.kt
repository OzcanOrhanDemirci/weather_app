package com.ozcanorhandemirci.hava.feature.cities

import com.ozcanorhandemirci.hava.core.model.WeatherError
import com.ozcanorhandemirci.hava.core.ui.CitySummary

/**
 * What the list of cities can be showing.
 *
 * The four states are distinct types rather than one object with flags, so a
 * screen cannot render a combination that was never meant to exist, such as an
 * error and a list at the same time.
 *
 * The one combination that does exist is deliberate: content that is on the
 * device together with a refresh that failed. That is not an error state. The
 * weather is still true, only older than intended, and replacing it with an
 * apology would take away the one thing the reader came for.
 */
sealed interface CitiesUiState {

    /** Nothing is known yet, including whether there are any cities. */
    data object Loading : CitiesUiState

    /** There are no cities to show at all. */
    data object Empty : CitiesUiState

    /** Nothing is on the device and nothing could be fetched. */
    data class Failed(val reason: WeatherError) : CitiesUiState

    data class Content(
        val cities: List<CitySummary>,
        val isRefreshing: Boolean,
        /** A refresh that failed while content was already on the screen. */
        val problem: WeatherError?,
    ) : CitiesUiState
}
