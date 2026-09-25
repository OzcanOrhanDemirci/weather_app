package com.ozcanorhandemirci.hava.core.network

/** Addresses and query shapes of the Open-Meteo service. */
internal object OpenMeteo {

    const val FORECAST_URL = "https://api.open-meteo.com/v1/forecast"
    const val GEOCODING_URL = "https://geocoding-api.open-meteo.com/v1/search"

    /**
     * Timestamps are requested as Unix seconds rather than as local strings.
     *
     * The alternative returns an hour as 2026-09-25T14:00 with no offset, which
     * has to be paired with a separate time zone field before it means
     * anything. A Unix second is already unambiguous, and the offset is applied
     * once, when the hour is shown.
     */
    const val TIME_FORMAT = "unixtime"

    /** A week is what the daily strip shows, and the hourly strip draws from the same window. */
    const val FORECAST_DAYS = 7

    val CURRENT_FIELDS = listOf(
        "temperature_2m",
        "apparent_temperature",
        "weather_code",
        "is_day",
        "wind_speed_10m",
        "wind_direction_10m",
        "wind_gusts_10m",
        "relative_humidity_2m",
        "surface_pressure",
    )

    val HOURLY_FIELDS = listOf(
        "temperature_2m",
        "apparent_temperature",
        "weather_code",
        "precipitation_probability",
        "precipitation",
        "wind_speed_10m",
        "is_day",
    )

    val DAILY_FIELDS = listOf(
        "weather_code",
        "temperature_2m_max",
        "temperature_2m_min",
        "sunrise",
        "sunset",
        "precipitation_probability_max",
        "uv_index_max",
    )
}
