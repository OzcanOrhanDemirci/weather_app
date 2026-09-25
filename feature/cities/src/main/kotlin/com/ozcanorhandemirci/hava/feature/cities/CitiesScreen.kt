package com.ozcanorhandemirci.hava.feature.cities

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.ozcanorhandemirci.hava.core.designsystem.theme.SkyPalette
import com.ozcanorhandemirci.hava.core.model.WeatherError
import com.ozcanorhandemirci.hava.core.sky.Sky
import com.ozcanorhandemirci.hava.core.sky.SkyConditions
import com.ozcanorhandemirci.hava.core.sky.SkyDetail
import com.ozcanorhandemirci.hava.core.sky.animateSkyPalette
import com.ozcanorhandemirci.hava.core.sky.rememberSkyState
import kotlin.math.abs

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
 * it: the sky, the colour of the text, the tint of every surface. That is the
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
    val listState = rememberLazyListState()
    val focused = (state as? CitiesUiState.Content)?.cities?.focusedBy(listState)
    val backdrop = focused?.skyConditions()

    val skyState = backdrop?.let { rememberSkyState(it) }
    val palette = animateSkyPalette(skyState?.palette ?: SkyPalette.Placeholder)

    HavaTheme(palette = palette) {
        Box(modifier = modifier.fillMaxSize()) {
            Crossfade(
                targetState = skyState,
                label = "backdrop",
                modifier = Modifier.fillMaxSize(),
            ) { sky ->
                if (sky != null) {
                    Sky(
                        state = sky,
                        detail = SkyDetail.Backdrop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

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
                        listState = listState,
                        onCitySelected = onCitySelected,
                        onRetry = onRefresh,
                    )
                    StatusBarVeil(modifier = Modifier.align(Alignment.TopCenter))
                }
            }
        }
    }
}

/**
 * Keeps the clock and the battery readable while the sky stays behind them.
 *
 * The window is drawn edge to edge because the sky belongs there, but a list
 * scrolls underneath the status bar, and a white city name arriving behind a
 * white clock makes both unreadable. A short fade of the sky colour separates
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
    listState: LazyListState,
    onCitySelected: (Long) -> Unit,
    onRetry: () -> Unit,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = HavaSpacing.gutter,
            end = HavaSpacing.gutter,
            bottom = HavaSpacing.section,
        ),
        verticalArrangement = Arrangement.spacedBy(HavaSpacing.compact),
    ) {
        item(key = "header") {
            Header(modifier = Modifier.statusBarsPadding().padding(bottom = HavaSpacing.small))
        }

        if (state.problem != null) {
            item(key = "problem") {
                ProblemBanner(reason = state.problem, onRetry = onRetry)
            }
        }

        items(items = state.cities, key = { it.city.id }) { item ->
            CityCard(item = item, onClick = { onCitySelected(item.city.id) })
        }
    }
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
 * Shown when a refresh failed but the device still holds a forecast.
 *
 * It sits above the list rather than replacing it, because the weather on
 * screen is still true. Only its age has changed.
 */
@Composable
private fun ProblemBanner(reason: WeatherError, onRetry: () -> Unit) {
    GlassSurface(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(HavaSpacing.tiny)) {
            Text(
                text = stringResource(reason.headline()),
                style = MaterialTheme.typography.titleMedium,
                color = HavaTheme.sky.content,
            )
            Text(
                text = stringResource(R.string.cities_problem_showing_stored),
                style = MaterialTheme.typography.bodyMedium,
                color = HavaTheme.sky.contentMuted,
            )
            TextButton(onClick = onRetry) {
                Text(text = stringResource(R.string.action_try_again))
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
    Box(modifier = Modifier.fillMaxSize().padding(HavaSpacing.section), contentAlignment = Alignment.Center) {
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
        modifier = Modifier.fillMaxSize().padding(HavaSpacing.gutter).navigationBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        GlassSurface {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(HavaSpacing.small),
            ) {
                Text(
                    text = stringResource(reason.headline()),
                    style = MaterialTheme.typography.headlineSmall,
                    color = HavaTheme.sky.content,
                )
                Text(
                    text = stringResource(reason.advice()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = HavaTheme.sky.contentMuted,
                )
                TextButton(onClick = onRetry) {
                    Text(text = stringResource(R.string.action_try_again))
                }
            }
        }
    }
}

/**
 * The card nearest the middle of the viewport.
 *
 * Nearest the middle rather than first visible, because a list scrolled to rest
 * between two cards should settle on the one a reader is actually looking at.
 */
@Composable
private fun List<CityWeather>.focusedBy(listState: LazyListState): CityWeather? {
    val cities = this
    val index by remember(cities) {
        derivedStateOf {
            val layout = listState.layoutInfo
            val middle = (layout.viewportStartOffset + layout.viewportEndOffset) / 2
            layout.visibleItemsInfo
                .filter { it.key is Long }
                .minByOrNull { abs(it.offset + it.size / 2 - middle) }
                ?.key as? Long
        }
    }

    return index?.let { id -> cities.firstOrNull { it.city.id == id } } ?: cities.firstOrNull()
}

private fun CityWeather.skyConditions(): SkyConditions? {
    val current = forecast?.snapshot?.current ?: return null
    return SkyConditions(
        kind = current.kind,
        instant = current.observedAt,
        coordinates = city.coordinates,
        windSpeedKph = current.wind.speedKph,
        windDirectionDegrees = current.wind.directionDegrees,
    )
}

/** How far the fade reaches past the status bar itself. */
private val VEIL_OVERHANG = 12.dp
private const val VEIL_HOLD = 0.6f
private const val VEIL_HOLD_ALPHA = 0.75f

private fun WeatherError.headline(): Int = when (this) {
    WeatherError.Offline -> R.string.error_offline_title
    WeatherError.Timeout -> R.string.error_timeout_title
    is WeatherError.Service -> R.string.error_service_title
    WeatherError.Unreadable -> R.string.error_unreadable_title
    WeatherError.UnknownPlace -> R.string.error_unknown_place_title
    is WeatherError.Unexpected -> R.string.error_unexpected_title
}

private fun WeatherError.advice(): Int = when (this) {
    WeatherError.Offline -> R.string.error_offline_body
    WeatherError.Timeout -> R.string.error_timeout_body
    is WeatherError.Service -> R.string.error_service_body
    WeatherError.Unreadable -> R.string.error_unreadable_body
    WeatherError.UnknownPlace -> R.string.error_unknown_place_body
    is WeatherError.Unexpected -> R.string.error_unexpected_body
}
