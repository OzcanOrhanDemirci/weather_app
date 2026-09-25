package com.ozcanorhandemirci.hava.feature.cities

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ozcanorhandemirci.hava.core.designsystem.component.GlassSurface
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaSpacing
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaTheme
import com.ozcanorhandemirci.hava.core.model.WeatherError
import com.ozcanorhandemirci.hava.core.sky.SkyConditions
import com.ozcanorhandemirci.hava.core.ui.CityGrid
import com.ozcanorhandemirci.hava.core.ui.CitySummary
import com.ozcanorhandemirci.hava.core.ui.SkyOf
import com.ozcanorhandemirci.hava.core.ui.adviceResource
import com.ozcanorhandemirci.hava.core.ui.headlineResource
import kotlin.math.abs
import kotlinx.coroutines.delay
import com.ozcanorhandemirci.hava.core.ui.R as UiR

@Composable
fun CitiesRoute(
    onCitySelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CitiesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CitiesScreen(
        state = state,
        onRefresh = viewModel::refresh,
        onCitySelected = onCitySelected,
        modifier = modifier,
    )
}

/**
 * The list of cities.
 *
 * The backdrop belongs to whichever card is nearest the middle of the screen,
 * so scrolling from the coast to the mountains carries the whole interface with
 * it: the sky, the color of the text, the tint of every surface. That is the
 * point of deriving the palette from the weather rather than fixing it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CitiesScreen(
    state: CitiesUiState,
    onRefresh: () -> Unit,
    onCitySelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gridState = rememberLazyGridState()
    val following = (state as? CitiesUiState.Content)?.cities?.skyFollowing(gridState)

    // The backdrop belongs to the card nearest the middle of the viewport, so
    // scrolling from the coast to the mountains carries the whole interface
    // with it rather than only the row under the finger.
    SkyOf(following?.skyConditions())

    Box(modifier = modifier.fillMaxSize()) {
        when (state) {
            CitiesUiState.Loading -> LoadingState()
            CitiesUiState.Empty -> EmptyState()
            is CitiesUiState.Failed -> FailedState(reason = state.reason, onRetry = onRefresh)
            is CitiesUiState.Content -> PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                CityList(
                    state = state,
                    gridState = gridState,
                    onCitySelected = onCitySelected,
                    onRetry = onRefresh,
                )
                StatusBarVeil(modifier = Modifier.align(Alignment.TopCenter))
            }
        }
    }
}

/**
 * Keeps the clock and the battery readable while the sky stays behind them.
 *
 * The window is drawn edge to edge because the sky belongs there, but a list
 * scrolls underneath the status bar, and a white city name arriving behind a
 * white clock makes both unreadable. A short fade of the sky color separates
 * them without putting a bar across the top of the screen.
 */
@Composable
private fun StatusBarVeil(modifier: Modifier = Modifier) {
    val height = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + VEIL_OVERHANG
    val sky = HavaTheme.sky

    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                Brush.verticalGradient(
                    0f to sky.zenith,
                    VEIL_HOLD to sky.zenith.copy(alpha = VEIL_HOLD_ALPHA),
                    1f to Color.Transparent,
                ),
            ),
    )
}

@Composable
private fun CityList(
    state: CitiesUiState.Content,
    gridState: LazyGridState,
    onCitySelected: (Long) -> Unit,
    onRetry: () -> Unit,
) {
    val problem = state.problem

    // Whether anything was stored decides what the banner may claim, and the
    // cities are the only place that knows. A city with no snapshot has never
    // had a reading on this device.
    val hasStoredWeather = state.cities.any { it.snapshot != null }

    val banner: (@Composable () -> Unit)? = problem?.let {
        { ProblemBanner(reason = it, hasStoredWeather = hasStoredWeather, onRetry = onRetry) }
    }

    CityGrid(
        cities = state.cities,
        onCitySelected = onCitySelected,
        state = gridState,
        banner = banner,
        header = {
            Header(modifier = Modifier.statusBarsPadding().padding(bottom = HavaSpacing.small))
        },
    )
}

@Composable
private fun Header(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(top = HavaSpacing.large),
        verticalArrangement = Arrangement.spacedBy(HavaSpacing.tiny),
    ) {
        Text(
            text = stringResource(R.string.cities_title),
            style = MaterialTheme.typography.displaySmall,
            color = HavaTheme.sky.content,
        )
        Text(
            text = stringResource(R.string.cities_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = HavaTheme.sky.contentMuted,
        )
    }
}

/**
 * Shown when a refresh failed.
 *
 * It sits above the list rather than replacing it, because when the device
 * holds a forecast the weather on screen is still true and only its age has
 * changed.
 *
 * The second line is not always that, though. On a first run with no network
 * there is nothing stored, and a banner saying so over twenty cards that all
 * read "waiting for a reading" is a sentence contradicted by everything under
 * it. A reader who is told something plainly false about what they are looking
 * at has no reason to believe the rest of the screen.
 */
@Composable
private fun ProblemBanner(
    reason: WeatherError,
    hasStoredWeather: Boolean,
    onRetry: () -> Unit,
) {
    GlassSurface(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(HavaSpacing.tiny)) {
            Text(
                text = stringResource(reason.headlineResource()),
                style = MaterialTheme.typography.titleMedium,
                color = HavaTheme.sky.content,
            )
            Text(
                text = stringResource(
                    if (hasStoredWeather) {
                        R.string.cities_problem_showing_stored
                    } else {
                        R.string.cities_problem_nothing_stored
                    },
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = HavaTheme.sky.contentMuted,
            )
            TextButton(onClick = onRetry) {
                Text(text = stringResource(UiR.string.action_try_again))
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = HavaTheme.sky.content)
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize().padding(HavaSpacing.section),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.cities_empty_title),
                style = MaterialTheme.typography.headlineSmall,
                color = HavaTheme.sky.content,
            )
            Text(
                text = stringResource(R.string.cities_empty_body),
                style = MaterialTheme.typography.bodyLarge,
                color = HavaTheme.sky.contentMuted,
            )
        }
    }
}

@Composable
private fun FailedState(reason: WeatherError, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(HavaSpacing.gutter),
        contentAlignment = Alignment.Center,
    ) {
        GlassSurface {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(HavaSpacing.small),
            ) {
                Text(
                    text = stringResource(reason.headlineResource()),
                    style = MaterialTheme.typography.headlineSmall,
                    color = HavaTheme.sky.content,
                )
                Text(
                    text = stringResource(reason.adviceResource()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = HavaTheme.sky.contentMuted,
                )
                TextButton(onClick = onRetry) {
                    Text(text = stringResource(UiR.string.action_try_again))
                }
            }
        }
    }
}

/**
 * The card the sky should belong to.
 *
 * Nearest the middle of the viewport rather than first visible, because a list
 * at rest between two cards should settle on the one being looked at.
 *
 * And only once the list has stopped moving past it. Following the middle of a
 * flick directly means the backdrop is handed a new place every few frames,
 * and a sky that takes two seconds to arrive spends the whole flick being
 * restarted: the weather never resolves and the screen flickers through a dozen
 * half finished transitions. Waiting for the focus to hold still turns a fast
 * scroll into one clean change at the end of it, while a slow scroll still
 * follows, because a card under a slow finger holds the middle for longer than
 * this.
 */
@Composable
private fun List<CitySummary>.skyFollowing(gridState: LazyGridState): CitySummary? {
    val cities = this
    val focusedId by remember(cities) {
        derivedStateOf {
            val layout = gridState.layoutInfo
            val middle = (layout.viewportStartOffset + layout.viewportEndOffset) / 2
            layout.visibleItemsInfo
                .filter { it.key is Long }
                // Only the distance down the list counts. Where a card sits
                // across a row of two says nothing about which weather is being
                // looked at, and the first of a tied row is as good an answer
                // as the second.
                .minByOrNull { abs(it.offset.y + it.size.height / 2 - middle) }
                ?.key as? Long
        }
    }

    var settledId by remember { mutableStateOf(focusedId) }
    LaunchedEffect(focusedId) {
        delay(SETTLE_MILLIS)
        settledId = focusedId
    }

    return settledId?.let { id -> cities.firstOrNull { it.city.id == id } } ?: cities.firstOrNull()
}

private fun CitySummary.skyConditions(): SkyConditions? {
    val current = snapshot?.current ?: return null
    return SkyConditions(
        kind = current.kind,
        instant = current.observedAt,
        coordinates = city.coordinates,
        windSpeedKph = current.wind.speedKph,
        windDirectionDegrees = current.wind.directionDegrees,
    )
}

/**
 * How long the focus has to hold still before the sky moves to it.
 *
 * Short enough that a deliberate scroll feels followed, long enough that a
 * flick past six cities is one change rather than six.
 */
private const val SETTLE_MILLIS = 400L

/** How far the fade reaches past the status bar itself. */
private val VEIL_OVERHANG = 12.dp
private const val VEIL_HOLD = 0.6f
private const val VEIL_HOLD_ALPHA = 0.75f
