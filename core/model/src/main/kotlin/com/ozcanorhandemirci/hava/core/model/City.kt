package com.ozcanorhandemirci.hava.core.model

/**
 * A place the application can show weather for.
 *
 * [id] is the Open-Meteo geocoding identifier, which stays stable across
 * sessions and therefore doubles as the local storage key.
 */
data class City(
    val id: Long,
    val name: String,
    val region: String?,
    val countryCode: String,
    val coordinates: Coordinates,
    val timeZoneId: String,
)
