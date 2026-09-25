package com.ozcanorhandemirci.hava.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import com.ozcanorhandemirci.hava.core.model.Intensity
import com.ozcanorhandemirci.hava.core.model.WeatherCondition
import com.ozcanorhandemirci.hava.core.model.WeatherKind

/**
 * The words for a condition.
 *
 * There are no weather icons anywhere in this application. The card behind the
 * text is already the sky of that place, drawn from the same reading, so a
 * small picture of a cloud on top of an actual cloud would say nothing the
 * screen has not said better. What is left for text is the part a picture is
 * bad at: how much of it there is.
 */
@Composable
@ReadOnlyComposable
fun WeatherKind.describe(): String = stringResource(descriptionResource())

private fun WeatherKind.descriptionResource(): Int = when (condition) {
    WeatherCondition.CLEAR -> R.string.weather_clear
    WeatherCondition.MAINLY_CLEAR -> R.string.weather_mainly_clear
    WeatherCondition.PARTLY_CLOUDY -> R.string.weather_partly_cloudy
    WeatherCondition.OVERCAST -> R.string.weather_overcast
    WeatherCondition.FOG -> R.string.weather_fog

    WeatherCondition.DRIZZLE -> when (intensity) {
        Intensity.HEAVY -> R.string.weather_drizzle_heavy
        else -> R.string.weather_drizzle
    }

    WeatherCondition.FREEZING_DRIZZLE, WeatherCondition.FREEZING_RAIN ->
        R.string.weather_freezing_rain

    WeatherCondition.RAIN -> when (intensity) {
        Intensity.LIGHT -> R.string.weather_rain_light
        Intensity.HEAVY -> R.string.weather_rain_heavy
        else -> R.string.weather_rain
    }

    WeatherCondition.RAIN_SHOWER -> when (intensity) {
        Intensity.HEAVY -> R.string.weather_shower_heavy
        else -> R.string.weather_shower
    }

    WeatherCondition.SNOW, WeatherCondition.SNOW_SHOWER -> when (intensity) {
        Intensity.LIGHT -> R.string.weather_snow_light
        Intensity.HEAVY -> R.string.weather_snow_heavy
        else -> R.string.weather_snow
    }

    WeatherCondition.SNOW_GRAINS -> R.string.weather_snow_grains
    WeatherCondition.THUNDERSTORM -> R.string.weather_thunderstorm
    WeatherCondition.THUNDERSTORM_WITH_HAIL -> R.string.weather_thunderstorm_hail
    WeatherCondition.UNKNOWN -> R.string.weather_unknown
}
