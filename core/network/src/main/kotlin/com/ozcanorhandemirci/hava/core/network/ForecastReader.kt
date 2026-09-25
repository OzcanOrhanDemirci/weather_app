package com.ozcanorhandemirci.hava.core.network

import com.ozcanorhandemirci.hava.core.model.City
import com.ozcanorhandemirci.hava.core.model.CurrentWeather
import com.ozcanorhandemirci.hava.core.model.DailyPoint
import com.ozcanorhandemirci.hava.core.model.HourlyPoint
import com.ozcanorhandemirci.hava.core.model.Outcome
import com.ozcanorhandemirci.hava.core.model.Temperature
import com.ozcanorhandemirci.hava.core.model.WeatherError
import com.ozcanorhandemirci.hava.core.model.WeatherKind
import com.ozcanorhandemirci.hava.core.model.WeatherSnapshot
import com.ozcanorhandemirci.hava.core.model.Wind
import com.ozcanorhandemirci.hava.core.network.dto.DailyDto
import com.ozcanorhandemirci.hava.core.network.dto.ForecastDto
import com.ozcanorhandemirci.hava.core.network.dto.HourlyDto
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json

/**
 * Turns a stored or freshly received forecast document into the domain.
 *
 * This is the only place the wire format is understood. A document read back
 * from the cache goes through exactly the same code as one that has just
 * arrived, so the two can never drift apart.
 */
@Singleton
class ForecastReader @Inject constructor(
    private val json: Json,
) {

    fun read(document: ForecastDocument, city: City, retrievedAt: Instant): Outcome<WeatherSnapshot> =
        try {
            val dto = json.decodeFromString(ForecastDto.serializer(), document.json)
            Outcome.Success(dto.toSnapshot(city, retrievedAt))
        } catch (failure: IllegalArgumentException) {
            // Both malformed input and a missing required field arrive as this.
            Outcome.Failure(WeatherError.Unreadable)
        }
}

private fun ForecastDto.toSnapshot(city: City, retrievedAt: Instant): WeatherSnapshot {
    val zone = resolveZone(timezone, utcOffsetSeconds)

    return WeatherSnapshot(
        city = city,
        current = CurrentWeather(
            observedAt = Instant.ofEpochSecond(current.time),
            temperature = Temperature.ofCelsius(current.temperature),
            apparentTemperature = Temperature.ofCelsius(current.apparentTemperature),
            kind = WeatherKind.fromWmoCode(current.weatherCode),
            isDay = current.isDay == 1,
            wind = Wind(
                speedKph = current.windSpeed,
                directionDegrees = current.windDirection,
                gustKph = current.windGusts,
            ),
            relativeHumidityPercent = current.relativeHumidity,
            pressureHpa = current.surfacePressure,
        ),
        hourly = hourly.toPoints(),
        daily = daily.toPoints(zone),
        retrievedAt = retrievedAt,
    )
}

/**
 * The service sends one array per variable, all aligned with the array of
 * times. They are always the same length; taking the shortest anyway means a
 * truncated response produces fewer hours rather than an exception.
 */
private fun HourlyDto.toPoints(): List<HourlyPoint> {
    val count = listOf(
        time.size,
        temperature.size,
        apparentTemperature.size,
        weatherCode.size,
    ).min()

    return List(count) { index ->
        HourlyPoint(
            time = Instant.ofEpochSecond(time[index]),
            temperature = Temperature.ofCelsius(temperature[index]),
            apparentTemperature = Temperature.ofCelsius(apparentTemperature[index]),
            kind = WeatherKind.fromWmoCode(weatherCode[index]),
            precipitationProbabilityPercent = precipitationProbability.getOrNull(index) ?: 0,
            precipitationMillimetres = precipitation.getOrNull(index) ?: 0.0,
            windSpeedKph = windSpeed.getOrNull(index) ?: 0.0,
            isDay = isDay.getOrNull(index) != 0,
        )
    }
}

private fun DailyDto.toPoints(zone: ZoneId): List<DailyPoint> {
    val count = listOf(
        time.size,
        weatherCode.size,
        maximum.size,
        minimum.size,
        sunrise.size,
        sunset.size,
    ).min()

    return List(count) { index ->
        DailyPoint(
            date = Instant.ofEpochSecond(time[index]).atZone(zone).toLocalDate(),
            kind = WeatherKind.fromWmoCode(weatherCode[index]),
            maximum = Temperature.ofCelsius(maximum[index]),
            minimum = Temperature.ofCelsius(minimum[index]),
            sunrise = Instant.ofEpochSecond(sunrise[index]),
            sunset = Instant.ofEpochSecond(sunset[index]),
            precipitationProbabilityPercent = precipitationProbabilityMax.getOrNull(index) ?: 0,
            uvIndexMax = uvIndexMax.getOrNull(index) ?: 0.0,
        )
    }
}

/**
 * Falls back to the plain offset when the named zone is not one this device
 * knows. A device with an old time zone database should show a day boundary an
 * hour out rather than no forecast at all.
 */
private fun resolveZone(timezone: String, utcOffsetSeconds: Int): ZoneId =
    runCatching { ZoneId.of(timezone) }.getOrElse { ZoneOffset.ofTotalSeconds(utcOffsetSeconds) }
