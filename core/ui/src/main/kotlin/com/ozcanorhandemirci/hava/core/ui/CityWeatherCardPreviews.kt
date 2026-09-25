package com.ozcanorhandemirci.hava.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaSpacing
import com.ozcanorhandemirci.hava.core.model.City
import com.ozcanorhandemirci.hava.core.model.Coordinates
import com.ozcanorhandemirci.hava.core.model.CurrentWeather
import com.ozcanorhandemirci.hava.core.model.Temperature
import com.ozcanorhandemirci.hava.core.model.WeatherKind
import com.ozcanorhandemirci.hava.core.model.WeatherSnapshot
import com.ozcanorhandemirci.hava.core.model.Wind
import java.time.Instant

/**
 * The card under the conditions that break a layout.
 *
 * A short name and a long one are the pair worth looking at, because the row
 * has to hold a name, a mark, a description and a temperature on one line, and
 * a name is the only one of those whose length nobody controls. The geocoding
 * service returns whatever a place is called, and some of them are long.
 *
 * The other two answer the questions a device asks rather than a designer: what
 * happens on a narrow screen, and what happens when the reader has asked for
 * large text.
 */
@Preview(name = "Short name", showBackground = true, widthDp = 400)
@Composable
private fun CityWeatherCardShortName() {
    PreviewCard(summary = shortNamed)
}

@Preview(name = "Long name", showBackground = true, widthDp = 400)
@Composable
private fun CityWeatherCardLongName() {
    PreviewCard(summary = longNamed)
}

@Preview(name = "Narrow screen", showBackground = true, widthDp = 320)
@Composable
private fun CityWeatherCardNarrow() {
    PreviewCard(summary = longNamed)
}

@Preview(name = "Large text", showBackground = true, widthDp = 400, fontScale = 1.8f)
@Composable
private fun CityWeatherCardLargeText() {
    PreviewCard(summary = longNamed)
}

@Preview(name = "Kept and waiting", showBackground = true, widthDp = 400)
@Composable
private fun CityWeatherCardStates() {
    Column(
        modifier = Modifier.padding(HavaSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(HavaSpacing.compact),
    ) {
        CityWeatherCard(summary = shortNamed.copy(isFavorite = true), onClick = {})
        CityWeatherCard(summary = shortNamed.copy(snapshot = null), onClick = {})
    }
}

@Composable
private fun PreviewCard(summary: CitySummary) {
    CityWeatherCard(
        summary = summary,
        onClick = {},
        modifier = Modifier.fillMaxWidth().padding(HavaSpacing.medium),
    )
}

private val shortNamed = previewSummary(name = "Rize", region = "Rize", celsius = 14.2)

/** Longer than anything the seed list contains, and plausible for a search result. */
private val longNamed = previewSummary(
    name = "Muhammedşerifzade Hacıbekirağa Çiftliği",
    region = "Muğla",
    celsius = 31.6,
    wmoCode = 3,
)

private fun previewSummary(
    name: String,
    region: String,
    celsius: Double,
    wmoCode: Int = 0,
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
                wind = Wind(speedKph = 11.0, directionDegrees = 210, gustKph = 18.0),
                relativeHumidityPercent = 48,
                pressureHpa = 1013.0,
            ),
            hourly = emptyList(),
            daily = emptyList(),
            retrievedAt = observedAt,
        ),
        isFavorite = false,
    )
}
