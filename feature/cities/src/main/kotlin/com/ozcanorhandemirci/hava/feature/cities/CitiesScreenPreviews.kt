package com.ozcanorhandemirci.hava.feature.cities

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaTheme
import com.ozcanorhandemirci.hava.core.designsystem.theme.SkyPalette
import com.ozcanorhandemirci.hava.core.model.City
import com.ozcanorhandemirci.hava.core.model.Coordinates
import com.ozcanorhandemirci.hava.core.model.CurrentWeather
import com.ozcanorhandemirci.hava.core.model.Temperature
import com.ozcanorhandemirci.hava.core.model.WeatherError
import com.ozcanorhandemirci.hava.core.model.WeatherKind
import com.ozcanorhandemirci.hava.core.model.WeatherSnapshot
import com.ozcanorhandemirci.hava.core.model.Wind
import com.ozcanorhandemirci.hava.core.ui.CitySummary
import java.time.Instant

/**
 * The four states the list can be in, side by side.
 *
 * They are separate types in the source, which means the compiler can tell they
 * are exhaustive but nobody can tell whether they are all worth looking at. A
 * preview per state is the cheapest way to keep the empty and failed screens
 * honest: both are difficult to reach by using the application, and a state
 * that is hard to reach is a state that rots.
 */
@Preview(name = "Content", showBackground = true)
@Composable
private fun CitiesContent() {
    PreviewScreen(
        CitiesUiState.Content(
            cities = listOf(izmir, rize.copy(isFavorite = true), waiting),
            isRefreshing = false,
            problem = null,
        ),
    )
}

/**
 * The combination the offline design exists for: weather on screen, and a
 * refresh that failed above it rather than instead of it.
 */
@Preview(name = "Content with a failed refresh", showBackground = true)
@Composable
private fun CitiesContentWithProblem() {
    PreviewScreen(
        CitiesUiState.Content(
            cities = listOf(izmir, rize),
            isRefreshing = false,
            problem = WeatherError.Offline,
        ),
    )
}

@Preview(name = "Loading", showBackground = true)
@Composable
private fun CitiesLoading() {
    PreviewScreen(CitiesUiState.Loading)
}

@Preview(name = "Empty", showBackground = true)
@Composable
private fun CitiesEmpty() {
    PreviewScreen(CitiesUiState.Empty)
}

@Preview(name = "Failed", showBackground = true)
@Composable
private fun CitiesFailed() {
    PreviewScreen(CitiesUiState.Failed(WeatherError.Offline))
}

@Preview(name = "Content, large text", showBackground = true, fontScale = 1.8f)
@Composable
private fun CitiesContentLargeText() {
    PreviewScreen(
        CitiesUiState.Content(
            cities = listOf(izmir, rize.copy(isFavorite = true)),
            isRefreshing = false,
            problem = null,
        ),
    )
}

@Composable
private fun PreviewScreen(state: CitiesUiState) {
    HavaTheme(palette = SkyPalette.Placeholder) {
        CitiesScreen(state = state, onRefresh = {}, onCitySelected = {})
    }
}

private val izmir = previewSummary("İzmir", "İzmir", 23.4, wmoCode = 0)
private val rize = previewSummary("Rize", "Rize", 14.8, wmoCode = 65)
private val waiting = previewSummary("Ardahan", "Ardahan", 0.0, wmoCode = 0).copy(snapshot = null)

private fun previewSummary(
    name: String,
    region: String,
    celsius: Double,
    wmoCode: Int,
): CitySummary {
    val city = City(
        id = name.hashCode().toLong(),
        name = name,
        region = region,
        countryCode = "TR",
        coordinates = Coordinates(latitude = 38.4127, longitude = 27.1384),
        timeZoneId = "Europe/Istanbul",
    )
    val observedAt = Instant.parse("2026-09-25T09:00:00Z")

    return CitySummary(
        city = city,
        snapshot = WeatherSnapshot(
            city = city,
            current = CurrentWeather(
                observedAt = observedAt,
                temperature = Temperature.ofCelsius(celsius),
                apparentTemperature = Temperature.ofCelsius(celsius + 1),
                kind = WeatherKind.fromWmoCode(wmoCode),
                isDay = true,
                wind = Wind(speedKph = 14.0, directionDegrees = 200, gustKph = 22.0),
                relativeHumidityPercent = 55,
                pressureHpa = 1011.0,
            ),
            hourly = emptyList(),
            daily = emptyList(),
            retrievedAt = observedAt,
        ),
        isFavorite = false,
    )
}
