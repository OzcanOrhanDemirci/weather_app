package com.ozcanorhandemirci.hava.feature.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ozcanorhandemirci.hava.core.data.model.CachedForecast
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaTheme
import com.ozcanorhandemirci.hava.core.designsystem.theme.SkyPalette
import com.ozcanorhandemirci.hava.core.model.City
import com.ozcanorhandemirci.hava.core.model.Coordinates
import com.ozcanorhandemirci.hava.core.model.CurrentWeather
import com.ozcanorhandemirci.hava.core.model.DailyPoint
import com.ozcanorhandemirci.hava.core.model.HourlyPoint
import com.ozcanorhandemirci.hava.core.model.Temperature
import com.ozcanorhandemirci.hava.core.model.WeatherError
import com.ozcanorhandemirci.hava.core.model.WeatherKind
import com.ozcanorhandemirci.hava.core.model.WeatherSnapshot
import com.ozcanorhandemirci.hava.core.model.Wind
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.PI
import kotlin.math.sin

/**
 * The four states this screen can be in, and the shapes of window it can be in.
 *
 * Two of these cannot be reached by using the application at all. A city is
 * only unknown if it is opened from a link to something the device no longer
 * holds, and the failed state needs both an empty store and a service that will
 * not answer. They were written, they were never looked at, and a state nobody
 * looks at is a state that rots quietly until the one reader who reaches it
 * finds a blank screen.
 */
@Preview(name = "Content", showBackground = true)
@Composable
private fun CityDetailContent() {
    PreviewScreen(content())
}

/**
 * The combination the offline design exists for: a forecast on screen, and a
 * refresh that failed above it rather than instead of it.
 */
@Preview(name = "Content with a failed refresh", showBackground = true)
@Composable
private fun CityDetailContentWithProblem() {
    PreviewScreen(content(problem = WeatherError.Offline))
}

@Preview(name = "Loading", showBackground = true)
@Composable
private fun CityDetailLoading() {
    PreviewScreen(CityDetailUiState.Loading)
}

/** Opened from a link to a place the device no longer holds. */
@Preview(name = "Unknown place", showBackground = true)
@Composable
private fun CityDetailUnknown() {
    PreviewScreen(CityDetailUiState.Unknown)
}

/** Nothing stored, and nothing could be fetched. */
@Preview(name = "Failed", showBackground = true)
@Composable
private fun CityDetailFailed() {
    PreviewScreen(CityDetailUiState.Failed(city = izmir, reason = WeatherError.Offline))
}

@Preview(name = "Content, large text", showBackground = true, fontScale = 1.8f)
@Composable
private fun CityDetailLargeText() {
    PreviewScreen(content())
}

/**
 * Wide enough for two panes, which is the branch a turned phone takes.
 *
 * Worth a preview of its own because it is a different layout rather than the
 * same one with more room, and nothing else here would show it.
 */
@Preview(name = "Wide window", showBackground = true, widthDp = 840, heightDp = 420)
@Composable
private fun CityDetailWide() {
    PreviewScreen(content())
}

@Composable
private fun PreviewScreen(state: CityDetailUiState) {
    HavaTheme(palette = SkyPalette.Placeholder) {
        CityDetailScreen(
            state = state,
            onBack = {},
            onRefresh = {},
            onFavoriteChange = {},
        )
    }
}

private fun content(problem: WeatherError? = null) = CityDetailUiState.Content(
    city = izmir,
    forecast = CachedForecast(snapshot = snapshot, retrievedAt = OBSERVED_AT),
    isFavorite = true,
    isRefreshing = false,
    problem = problem,
)

private val OBSERVED_AT: Instant = Instant.parse("2026-09-25T09:00:00Z")
private val TURKEY: ZoneOffset = ZoneOffset.ofHours(3)

private val izmir = City(
    id = 311046L,
    name = "İzmir",
    region = "İzmir",
    countryCode = "TR",
    coordinates = Coordinates(latitude = 38.4127, longitude = 27.1384),
    timeZoneId = "Europe/Istanbul",
)

private val snapshot = WeatherSnapshot(
    city = izmir,
    current = CurrentWeather(
        observedAt = OBSERVED_AT,
        temperature = Temperature.ofCelsius(24.0),
        apparentTemperature = Temperature.ofCelsius(25.0),
        kind = WeatherKind.fromWmoCode(WMO_PARTLY_CLOUDY),
        isDay = true,
        wind = Wind(speedKph = 13.0, directionDegrees = 215, gustKph = 22.0),
        relativeHumidityPercent = 46,
        pressureHpa = 1014.0,
    ),
    hourly = hourly(),
    daily = daily(),
    retrievedAt = OBSERVED_AT,
)

/**
 * A day that rises and falls, rather than a flat line.
 *
 * The chart is the point of this screen, and a preview of it drawn from equal
 * numbers would show a straight line and prove nothing about the curve, the
 * labels or the reading that follows the finger.
 */
private fun hourly(): List<HourlyPoint> = List(HOURS) { index ->
    val turn = sin((index - HOURS_BEFORE_PEAK) * 2 * PI / HOURS_IN_DAY)
    val celsius = MEAN_CELSIUS + SWING_CELSIUS * turn
    val time = OBSERVED_AT.minusSeconds(SECONDS_IN_HOUR).plusSeconds(index * SECONDS_IN_HOUR)

    HourlyPoint(
        time = time,
        temperature = Temperature.ofCelsius(celsius),
        apparentTemperature = Temperature.ofCelsius(celsius + 1),
        kind = WeatherKind.fromWmoCode(if (turn < 0) WMO_OVERCAST else WMO_PARTLY_CLOUDY),
        precipitationProbabilityPercent = if (turn < 0) 40 else 5,
        precipitationMillimeters = if (turn < 0) 0.4 else 0.0,
        windSpeedKph = 11.0,
        isDay = turn > 0,
    )
}

private fun daily(): List<DailyPoint> = List(DAYS) { index ->
    val date = LocalDate.of(2026, 9, 25).plusDays(index.toLong())

    DailyPoint(
        date = date,
        kind = WeatherKind.fromWmoCode(if (index % 3 == 0) WMO_OVERCAST else WMO_PARTLY_CLOUDY),
        maximum = Temperature.ofCelsius(27.0 - index),
        minimum = Temperature.ofCelsius(15.0 + index % 3),
        sunrise = date.atTime(6, 51).toInstant(TURKEY),
        sunset = date.atTime(19, 6).toInstant(TURKEY),
        precipitationProbabilityPercent = index * 8,
        uvIndexMax = 6.0 - index * 0.5,
    )
}

private const val WMO_PARTLY_CLOUDY = 2
private const val WMO_OVERCAST = 3

/** One more than the screen shows, so the strip is never short of an hour. */
private const val HOURS = 30
private const val HOURS_IN_DAY = 24
private const val DAYS = 7

/** Puts the warmest hour in the afternoon rather than at the start of the list. */
private const val HOURS_BEFORE_PEAK = 6

private const val MEAN_CELSIUS = 21.0
private const val SWING_CELSIUS = 6.0
private const val SECONDS_IN_HOUR = 3_600L
