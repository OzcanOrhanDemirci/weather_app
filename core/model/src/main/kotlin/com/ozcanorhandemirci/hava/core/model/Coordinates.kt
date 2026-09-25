package com.ozcanorhandemirci.hava.core.model

/** A point on the globe in decimal degrees. */
data class Coordinates(
    val latitude: Double,
    val longitude: Double,
) {
    init {
        require(latitude in -90.0..90.0) { "Latitude out of range: $latitude" }
        require(longitude in -180.0..180.0) { "Longitude out of range: $longitude" }
    }
}
