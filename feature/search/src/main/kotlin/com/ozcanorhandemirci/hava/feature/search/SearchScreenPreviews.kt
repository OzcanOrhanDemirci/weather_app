package com.ozcanorhandemirci.hava.feature.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaTheme
import com.ozcanorhandemirci.hava.core.designsystem.theme.SkyPalette
import com.ozcanorhandemirci.hava.core.model.City
import com.ozcanorhandemirci.hava.core.model.Coordinates
import com.ozcanorhandemirci.hava.core.model.WeatherError

/**
 * The five states this screen can be in.
 *
 * Four of them are a line of text in the same place, which is exactly why they
 * are worth seeing side by side: nothing but a preview shows whether they read
 * as four different answers or as one message that keeps changing.
 */
@Preview(name = "Results", showBackground = true)
@Composable
private fun SearchResults() {
    PreviewScreen(query = "izmir", state = SearchUiState.Results(places = results))
}

/** Nothing typed yet, which is how every reader arrives. */
@Preview(name = "Idle", showBackground = true)
@Composable
private fun SearchIdle() {
    PreviewScreen(query = "", state = SearchUiState.Idle)
}

@Preview(name = "Searching", showBackground = true)
@Composable
private fun SearchSearching() {
    PreviewScreen(query = "izmir", state = SearchUiState.Searching)
}

/**
 * A real answer rather than a failure: the service replied, and there is no
 * such place.
 */
@Preview(name = "No matches", showBackground = true)
@Composable
private fun SearchNoMatches() {
    PreviewScreen(query = "qqqq", state = SearchUiState.NoMatches)
}

@Preview(name = "Failed", showBackground = true)
@Composable
private fun SearchFailed() {
    PreviewScreen(query = "izmir", state = SearchUiState.Failed(WeatherError.Offline))
}

@Preview(name = "Results, large text", showBackground = true, fontScale = 1.8f)
@Composable
private fun SearchResultsLargeText() {
    PreviewScreen(query = "izmir", state = SearchUiState.Results(places = results))
}

/** Wide enough to show that the column stops widening and sits in the middle. */
@Preview(name = "Wide window", showBackground = true, widthDp = 840, heightDp = 420)
@Composable
private fun SearchWide() {
    PreviewScreen(query = "izmir", state = SearchUiState.Results(places = results))
}

@Composable
private fun PreviewScreen(query: String, state: SearchUiState) {
    HavaTheme(palette = SkyPalette.Placeholder) {
        SearchScreen(
            query = query,
            state = state,
            onQueryChange = {},
            onPlaceSelected = {},
        )
    }
}

/**
 * What the geocoding service actually returns for a short query: the place that
 * was meant, and several that merely start the same way, in other provinces and
 * other countries. The row has to make them tellable apart.
 */
private val results = listOf(
    place(name = "İzmir", region = "İzmir", country = "TR"),
    place(name = "İzmirli", region = "Düzce", country = "TR"),
    place(name = "Izmira", region = "Haskovo", country = "BG"),
    place(name = "İzmir Karşıyaka", region = "İzmir", country = "TR"),
)

private fun place(name: String, region: String, country: String) = City(
    id = (name + region).hashCode().toLong(),
    name = name,
    region = region,
    countryCode = country,
    coordinates = Coordinates(latitude = 38.4127, longitude = 27.1384),
    timeZoneId = "Europe/Istanbul",
)
