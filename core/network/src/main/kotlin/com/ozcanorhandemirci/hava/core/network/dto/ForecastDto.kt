package com.ozcanorhandemirci.hava.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The shape of an Open-Meteo forecast.
 *
 * The service returns each variable as its own array, aligned with a shared
 * array of timestamps, rather than as a list of objects. These types follow
 * that shape exactly; turning it into something more convenient is the job of
 * the mapper, not of the parser.
 */
@Serializable
internal data class ForecastDto(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    @SerialName("utc_offset_seconds") val utcOffsetSeconds: Int,
    val current: CurrentDto,
    val hourly: HourlyDto,
    val daily: DailyDto,
)

@Serializable
internal data class CurrentDto(
    val time: Long,
    @SerialName("temperature_2m") val temperature: Double,
    @SerialName("apparent_temperature") val apparentTemperature: Double,
    @SerialName("weather_code") val weatherCode: Int,
    @SerialName("is_day") val isDay: Int,
    @SerialName("wind_speed_10m") val windSpeed: Double,
    @SerialName("wind_direction_10m") val windDirection: Int,
    @SerialName("wind_gusts_10m") val windGusts: Double? = null,
    @SerialName("relative_humidity_2m") val relativeHumidity: Int,
    @SerialName("surface_pressure") val surfacePressure: Double,
)

@Serializable
internal data class HourlyDto(
    val time: List<Long>,
    @SerialName("temperature_2m") val temperature: List<Double>,
    @SerialName("apparent_temperature") val apparentTemperature: List<Double>,
    @SerialName("weather_code") val weatherCode: List<Int>,
    @SerialName("precipitation_probability") val precipitationProbability: List<Int?> = emptyList(),
    val precipitation: List<Double> = emptyList(),
    @SerialName("wind_speed_10m") val windSpeed: List<Double> = emptyList(),
    @SerialName("is_day") val isDay: List<Int> = emptyList(),
)

@Serializable
internal data class DailyDto(
    val time: List<Long>,
    @SerialName("weather_code") val weatherCode: List<Int>,
    @SerialName("temperature_2m_max") val maximum: List<Double>,
    @SerialName("temperature_2m_min") val minimum: List<Double>,
    val sunrise: List<Long>,
    val sunset: List<Long>,
    @SerialName("precipitation_probability_max") val precipitationProbabilityMax: List<Int?> = emptyList(),
    @SerialName("uv_index_max") val uvIndexMax: List<Double?> = emptyList(),
)
