package com.ozcanorhandemirci.hava.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The shape of an Open-Meteo geocoding answer.
 *
 * A search that matches nothing omits the results field entirely rather than
 * returning an empty array, so it is declared nullable with a default. Treating
 * that case as a parse failure would turn a perfectly ordinary empty search
 * into an error screen.
 */
@Serializable
internal data class GeocodingDto(
    val results: List<PlaceDto>? = null,
)

@Serializable
internal data class PlaceDto(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    @SerialName("country_code") val countryCode: String? = null,
    val country: String? = null,
    val admin1: String? = null,
    val population: Long? = null,
)
