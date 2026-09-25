package com.ozcanorhandemirci.hava.core.ui

import androidx.annotation.StringRes
import com.ozcanorhandemirci.hava.core.model.WeatherError

/**
 * What a failure is called, and what to do about it.
 *
 * Held once rather than per screen. Two screens describing the same lost
 * connection differently is the sort of thing nobody notices while writing and
 * everybody notices while reading.
 */
@StringRes
fun WeatherError.headlineResource(): Int = when (this) {
    WeatherError.Offline -> R.string.error_offline_title
    WeatherError.Timeout -> R.string.error_timeout_title
    is WeatherError.Service -> R.string.error_service_title
    WeatherError.Unreadable -> R.string.error_unreadable_title
    WeatherError.UnknownPlace -> R.string.error_unknown_place_title
    is WeatherError.Unexpected -> R.string.error_unexpected_title
}

@StringRes
fun WeatherError.adviceResource(): Int = when (this) {
    WeatherError.Offline -> R.string.error_offline_body
    WeatherError.Timeout -> R.string.error_timeout_body
    is WeatherError.Service -> R.string.error_service_body
    WeatherError.Unreadable -> R.string.error_unreadable_body
    WeatherError.UnknownPlace -> R.string.error_unknown_place_body
    is WeatherError.Unexpected -> R.string.error_unexpected_body
}
