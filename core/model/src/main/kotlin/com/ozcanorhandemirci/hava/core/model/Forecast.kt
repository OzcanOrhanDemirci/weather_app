package com.ozcanorhandemirci.hava.core.model

import java.time.Instant
import java.time.LocalDate

/** Conditions at the moment the forecast was produced. */
data class CurrentWeather(
    val observedAt: Instant,
    val temperature: Temperature,
    val apparentTemperature: Temperature,
    val kind: WeatherKind,
    val isDay: Boolean,
    val wind: Wind,
    val relativeHumidityPercent: Int,
    val pressureHpa: Double,
)

/** A single hour of the hourly forecast. */
data class HourlyPoint(
    val time: Instant,
    val temperature: Temperature,
    val apparentTemperature: Temperature,
    val kind: WeatherKind,
    val precipitationProbabilityPercent: Int,
    val precipitationMillimetres: Double,
    val windSpeedKph: Double,
    val isDay: Boolean,
)

/** A single day of the daily forecast. */
data class DailyPoint(
    val date: LocalDate,
    val kind: WeatherKind,
    val maximum: Temperature,
    val minimum: Temperature,
    val sunrise: Instant,
    val sunset: Instant,
    val precipitationProbabilityPercent: Int,
    val uvIndexMax: Double,
)
