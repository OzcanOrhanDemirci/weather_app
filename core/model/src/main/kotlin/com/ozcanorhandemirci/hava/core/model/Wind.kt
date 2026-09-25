package com.ozcanorhandemirci.hava.core.model

/** Wind as reported by the forecast, in kilometers per hour and degrees from north. */
data class Wind(
    val speedKph: Double,
    val directionDegrees: Int,
    val gustKph: Double?,
) {
    /** Direction normalized to the range the backdrop uses to drift precipitation. */
    val directionRadians: Double
        get() = Math.toRadians(directionDegrees.toDouble())
}
