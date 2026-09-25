package com.ozcanorhandemirci.hava.core.ui

import androidx.compose.runtime.Immutable
import com.ozcanorhandemirci.hava.core.model.City
import com.ozcanorhandemirci.hava.core.model.WeatherSnapshot

/**
 * A place and whatever is known about it, in the form the shared card needs.
 *
 * Deliberately not the cached forecast itself: the card has no business with
 * when a reading arrived, and taking the cache type here would drag the whole
 * data layer into the shared presentation module.
 */
@Immutable
data class CitySummary(
    val city: City,
    val snapshot: WeatherSnapshot?,
    val isFavorite: Boolean,
)
