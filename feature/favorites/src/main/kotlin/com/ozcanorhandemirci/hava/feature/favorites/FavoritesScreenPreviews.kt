package com.ozcanorhandemirci.hava.feature.favorites

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaTheme
import com.ozcanorhandemirci.hava.core.designsystem.theme.SkyPalette
import com.ozcanorhandemirci.hava.core.model.City
import com.ozcanorhandemirci.hava.core.model.Coordinates
import com.ozcanorhandemirci.hava.core.model.CurrentWeather
import com.ozcanorhandemirci.hava.core.model.Temperature
import com.ozcanorhandemirci.hava.core.model.WeatherKind
import com.ozcanorhandemirci.hava.core.model.WeatherSnapshot
import com.ozcanorhandemirci.hava.core.model.Wind
import com.ozcanorhandemirci.hava.core.ui.CitySummary
import java.time.Instant

/**
 * The three states this screen can be in.
 *
 * The empty one carries the whole feature. Nobody arrives here with favourites
 * already kept, so it is the first thing every reader sees, and it is also the
 * screen that is hardest to get back to once anything has been favourited.
 */
@Preview(name = "Empty", showBackground = true)
@Composable
private fun FavoritesEmpty() {
    PreviewScreen(FavoritesUiState.Empty)
}

@Preview(name = "Content", showBackground = true)
@Composable
private fun FavoritesContent() {
    PreviewScreen(FavoritesUiState.Content(cities = listOf(izmir, rize, waiting)))
}

@Preview(name = "Loading", showBackground = true)
@Composable
private fun FavoritesLoading() {
    PreviewScreen(FavoritesUiState.Loading)
}

@Preview(name = "Empty, large text", showBackground = true, fontScale = 1.8f)
@Composable
private fun FavoritesEmptyLargeText() {
    PreviewScreen(FavoritesUiState.Empty)
}

/** Wide enough for the grid to take two columns, which is what a turned phone does. */
@Preview(name = "Wide window", showBackground = true, widthDp = 840, heightDp = 420)
@Composable
private fun FavoritesWide() {
    PreviewScreen(FavoritesUiState.Content(cities = listOf(izmir, rize, waiting)))
}

@Composable
private fun PreviewScreen(state: FavoritesUiState) {
    HavaTheme(palette = SkyPalette.Placeholder) {
        FavoritesScreen(state = state, onCitySelected = {})
    }
}

private val izmir = previewSummary("İzmir", "İzmir", celsius = 24.2, wmoCode = 0)
private val rize = previewSummary("Rize", "Rize", celsius = 14.8, wmoCode = 65)

/** Kept before its first reading arrived, which is a card with no number on it. */
private val waiting = previewSummary("Ardahan", "Ardahan", celsius = 0.0, wmoCode = 0)
    .copy(snapshot = null)

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
                wind = Wind(speedKph = 12.0, directionDegrees = 210, gustKph = 19.0),
                relativeHumidityPercent = 52,
                pressureHpa = 1012.0,
            ),
            hourly = emptyList(),
            daily = emptyList(),
            retrievedAt = observedAt,
        ),
        // Everything on this screen is kept, by definition.
        isFavorite = true,
    )
}
