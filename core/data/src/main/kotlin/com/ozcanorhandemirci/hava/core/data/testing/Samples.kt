package com.ozcanorhandemirci.hava.core.data.testing

import com.ozcanorhandemirci.hava.core.data.model.CachedForecast
import com.ozcanorhandemirci.hava.core.model.City
import com.ozcanorhandemirci.hava.core.model.Coordinates
import com.ozcanorhandemirci.hava.core.model.CurrentWeather
import com.ozcanorhandemirci.hava.core.model.Temperature
import com.ozcanorhandemirci.hava.core.model.WeatherKind
import com.ozcanorhandemirci.hava.core.model.WeatherSnapshot
import com.ozcanorhandemirci.hava.core.model.Wind
import java.time.Instant

/** Places and readings for tests, so no test has to invent its own. */
object Samples {

    val izmir = City(
        id = 311046L,
        name = "İzmir",
        region = "İzmir",
        countryCode = "TR",
        coordinates = Coordinates(38.4127, 27.1384),
        timeZoneId = "Europe/Istanbul",
    )

    val rize = City(
        id = 740483L,
        name = "Rize",
        region = "Rize",
        countryCode = "TR",
        coordinates = Coordinates(41.0208, 40.5219),
        timeZoneId = "Europe/Istanbul",
    )

    fun snapshot(city: City, celsius: Double = 21.0, wmoCode: Int = 0): WeatherSnapshot =
        WeatherSnapshot(
            city = city,
            current = CurrentWeather(
                observedAt = Instant.parse("2026-09-25T09:00:00Z"),
                temperature = Temperature.ofCelsius(celsius),
                apparentTemperature = Temperature.ofCelsius(celsius + 1),
                kind = WeatherKind.fromWmoCode(wmoCode),
                isDay = true,
                wind = Wind(speedKph = 12.0, directionDegrees = 220, gustKph = 20.0),
                relativeHumidityPercent = 40,
                pressureHpa = 1015.0,
            ),
            hourly = emptyList(),
            daily = emptyList(),
            retrievedAt = Instant.parse("2026-09-25T09:00:00Z"),
        )

    fun cached(city: City, celsius: Double = 21.0, wmoCode: Int = 0): CachedForecast =
        CachedForecast(
            snapshot = snapshot(city, celsius, wmoCode),
            retrievedAt = Instant.parse("2026-09-25T09:00:00Z"),
        )
}
