package com.ozcanorhandemirci.hava.feature.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ozcanorhandemirci.hava.core.designsystem.component.GlassSurface
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaMotion
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaSpacing
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaTheme
import com.ozcanorhandemirci.hava.core.designsystem.theme.respectingReducedMotion
import com.ozcanorhandemirci.hava.core.model.HourlyPoint
import com.ozcanorhandemirci.hava.core.model.WeatherError
import com.ozcanorhandemirci.hava.core.model.WeatherSnapshot
import com.ozcanorhandemirci.hava.core.sky.SkyConditions
import com.ozcanorhandemirci.hava.core.ui.SkyOf
import com.ozcanorhandemirci.hava.core.ui.describe
import com.ozcanorhandemirci.hava.core.ui.format
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun CityDetailRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CityDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CityDetailScreen(
        state = state,
        onBack = onBack,
        onRefresh = viewModel::refresh,
        onFavoriteChange = viewModel::setFavorite,
        modifier = modifier,
    )
}

/**
 * One city, in full.
 *
 * The screen is drawn over the sky of the hour being looked at rather than the
 * hour it happens to be. Dragging along the curve moves that hour, and with it
 * the light, the height of the sun and whatever is falling. The forecast is not
 * described; it is shown.
 */
@Composable
internal fun CityDetailScreen(
    state: CityDetailUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onFavoriteChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        CityDetailUiState.Loading -> Pending(modifier)
        CityDetailUiState.Unknown -> Missing(onBack = onBack, modifier = modifier)
        is CityDetailUiState.Failed -> Unavailable(
            reason = state.reason,
            onBack = onBack,
            onRetry = onRefresh,
            modifier = modifier,
        )

        is CityDetailUiState.Content -> Loaded(
            state = state,
            onBack = onBack,
            onRefresh = onRefresh,
            onFavoriteChange = onFavoriteChange,
            modifier = modifier,
        )
    }
}

@Composable
private fun Loaded(
    state: CityDetailUiState.Content,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onFavoriteChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snapshot = state.forecast.snapshot
    val zone = remember(state.city.timeZoneId) { state.city.zone() }
    val hours = remember(snapshot) { snapshot.comingHours() }

    var selected by rememberSaveable(state.city.id) { mutableIntStateOf(0) }
    val index = selected.coerceIn(hours.indices)
    val hour = hours.getOrNull(index)

    val conditions = remember(hour, state.city) {
        hour?.let {
            SkyConditions(
                kind = it.kind,
                instant = it.time,
                coordinates = state.city.coordinates,
                windSpeedKph = it.windSpeedKph,
                windDirectionDegrees = snapshot.current.wind.directionDegrees,
            )
        }
    } ?: return

    // Dragging the hourly curve moves this, and with it the light behind every
    // screen the reader can see.
    SkyOf(conditions)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = HavaSpacing.gutter),
            verticalArrangement = Arrangement.spacedBy(HavaSpacing.medium),
        ) {
            TopRow(
                title = state.city.name,
                subtitle = state.city.region,
                isFavorite = state.isFavorite,
                onBack = onBack,
                onFavoriteChange = onFavoriteChange,
            )

            Hero(
                hour = hour ?: return@Column,
                snapshot = snapshot,
                isNow = index == 0,
                zone = zone,
                onReturnToNow = { selected = 0 },
            )

            if (state.problem != null) {
                Problem(reason = state.problem, onRetry = onRefresh)
            }

            Section(title = stringResource(R.string.detail_hourly)) {
                HourlyCurve(
                    hours = hours,
                    zone = zone,
                    selectedIndex = index,
                    onSelect = { selected = it },
                )
            }

            snapshot.today?.let { today ->
                Section(title = stringResource(R.string.detail_sun)) {
                    SunArc(
                        sunrise = today.sunrise,
                        sunset = today.sunset,
                        now = snapshot.current.observedAt,
                        zone = zone,
                    )
                }
            }

            Section(title = stringResource(R.string.detail_week)) {
                DailyOutlook(days = snapshot.daily)
            }

            Section(title = stringResource(R.string.detail_conditions)) {
                Readings(snapshot = snapshot)
            }

            Column(modifier = Modifier.padding(bottom = HavaSpacing.section)) {
                Text(
                    text = stringResource(
                        R.string.detail_updated_at,
                        remember(zone) { TIME_FORMAT.withZone(zone) }.format(state.forecast.retrievedAt),
                    ),
                    style = HavaTheme.typography.overline,
                    color = HavaTheme.sky.contentMuted,
                )
            }
        }

        if (state.isRefreshing) {
            CircularProgressIndicator(
                color = HavaTheme.sky.content,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = HavaSpacing.small),
            )
        }
    }
}

@Composable
private fun TopRow(
    title: String,
    subtitle: String?,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onFavoriteChange: (Boolean) -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val palette = HavaTheme.sky

    // The heart swells as it is filled. A toggle that only changes colour is
    // read as information; one that moves is read as something the reader did.
    val emphasis by animateFloatAsState(
        targetValue = if (isFavorite) 1f else 0.86f,
        animationSpec = respectingReducedMotion(HavaMotion.expressive()),
        label = "favorite",
    )

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = HavaSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.detail_back),
                tint = palette.content,
            )
        }

        Column(modifier = Modifier.weight(1f).padding(start = HavaSpacing.small)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = palette.content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null && subtitle != title) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.contentMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        IconButton(
            onClick = {
                haptics.performHapticFeedback(
                    if (isFavorite) HapticFeedbackType.ToggleOff else HapticFeedbackType.ToggleOn,
                )
                onFavoriteChange(!isFavorite)
            },
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = stringResource(
                    if (isFavorite) R.string.detail_unfavorite else R.string.detail_favorite,
                ),
                tint = if (isFavorite) palette.accent else palette.content,
                modifier = Modifier.scale(emphasis),
            )
        }
    }
}

@Composable
private fun Hero(
    hour: HourlyPoint,
    snapshot: WeatherSnapshot,
    isNow: Boolean,
    zone: ZoneId,
    onReturnToNow: () -> Unit,
) {
    val palette = HavaTheme.sky
    val format = remember(zone) { TIME_FORMAT.withZone(zone) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = HavaSpacing.large),
        verticalArrangement = Arrangement.spacedBy(HavaSpacing.tiny),
    ) {
        Text(
            text = hour.temperature.format(),
            style = HavaTheme.typography.temperatureHero,
            color = palette.content,
        )
        Text(
            text = hour.kind.describe(),
            style = MaterialTheme.typography.titleLarge,
            color = palette.content,
        )
        Text(
            text = stringResource(R.string.detail_feels_like, hour.apparentTemperature.format()),
            style = MaterialTheme.typography.bodyLarge,
            color = palette.contentMuted,
        )

        // Only offered once the reader has moved away from the present.
        AnimatedVisibility(
            visible = !isNow,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut() + scaleOut(targetScale = 0.9f),
        ) {
            AssistChip(
                onClick = onReturnToNow,
                label = { Text(stringResource(R.string.detail_return_to_now, format.format(hour.time))) },
                colors = AssistChipDefaults.assistChipColors(
                    labelColor = palette.content,
                    containerColor = palette.glass,
                ),
                modifier = Modifier.padding(top = HavaSpacing.small),
            )
        }

        if (isNow) {
            Text(
                text = stringResource(
                    R.string.detail_today_range,
                    snapshot.today?.minimum?.format().orEmpty(),
                    snapshot.today?.maximum?.format().orEmpty(),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = palette.contentMuted,
                modifier = Modifier.padding(top = HavaSpacing.small),
            )
        }
    }
}

@Composable
private fun Readings(snapshot: WeatherSnapshot) {
    val current = snapshot.current

    Column(verticalArrangement = Arrangement.spacedBy(HavaSpacing.compact)) {
        Reading(stringResource(R.string.detail_wind), stringResource(R.string.detail_wind_value, current.wind.speedKph.toInt()))
        Reading(stringResource(R.string.detail_humidity), "${current.relativeHumidityPercent}%")
        Reading(stringResource(R.string.detail_pressure), stringResource(R.string.detail_pressure_value, current.pressureHpa.toInt()))
        snapshot.today?.let {
            Reading(stringResource(R.string.detail_uv), it.uvIndexMax.toInt().toString())
        }
    }
}

@Composable
private fun Reading(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = HavaTheme.sky.contentMuted)
        Text(text = value, style = HavaTheme.typography.metric, color = HavaTheme.sky.content)
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(HavaSpacing.small)) {
        Text(
            text = title,
            style = HavaTheme.typography.overline,
            color = HavaTheme.sky.contentMuted,
            modifier = Modifier.padding(start = HavaSpacing.tiny),
        )
        GlassSurface(modifier = Modifier.fillMaxWidth()) { content() }
    }
}

@Composable
private fun Problem(reason: WeatherError, onRetry: () -> Unit) {
    GlassSurface(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(HavaSpacing.tiny)) {
            Text(
                text = stringResource(reason.headline()),
                style = MaterialTheme.typography.titleMedium,
                color = HavaTheme.sky.content,
            )
            Text(
                text = stringResource(R.string.detail_showing_stored),
                style = MaterialTheme.typography.bodyMedium,
                color = HavaTheme.sky.contentMuted,
            )
            TextButton(onClick = onRetry) { Text(stringResource(R.string.detail_try_again)) }
        }
    }
}

@Composable
private fun Pending(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = HavaTheme.sky.content)
    }
}

@Composable
private fun Missing(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().padding(HavaSpacing.gutter), contentAlignment = Alignment.Center) {
        GlassSurface {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(HavaSpacing.small),
            ) {
                Text(
                    text = stringResource(R.string.detail_missing),
                    style = MaterialTheme.typography.titleMedium,
                    color = HavaTheme.sky.content,
                )
                TextButton(onClick = onBack) { Text(stringResource(R.string.detail_back)) }
            }
        }
    }
}

@Composable
private fun Unavailable(
    reason: WeatherError,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().padding(HavaSpacing.gutter), contentAlignment = Alignment.Center) {
        GlassSurface {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(HavaSpacing.small),
            ) {
                Text(
                    text = stringResource(reason.headline()),
                    style = MaterialTheme.typography.titleMedium,
                    color = HavaTheme.sky.content,
                )
                TextButton(onClick = onRetry) { Text(stringResource(R.string.detail_try_again)) }
                TextButton(onClick = onBack) { Text(stringResource(R.string.detail_back)) }
            }
        }
    }
}

/** The next day of the forecast, starting from the hour that is current. */
private fun WeatherSnapshot.comingHours(): List<HourlyPoint> =
    hourlyFrom(current.observedAt.minusSeconds(SECONDS_IN_HOUR), HOURS_SHOWN)

private fun com.ozcanorhandemirci.hava.core.model.City.zone(): ZoneId =
    runCatching { ZoneId.of(timeZoneId) }.getOrDefault(ZoneOffset.UTC)

private fun WeatherError.headline(): Int = when (this) {
    WeatherError.Offline -> R.string.detail_error_offline
    WeatherError.Timeout -> R.string.detail_error_timeout
    is WeatherError.Service -> R.string.detail_error_service
    WeatherError.Unreadable -> R.string.detail_error_unreadable
    WeatherError.UnknownPlace -> R.string.detail_error_unknown_place
    is WeatherError.Unexpected -> R.string.detail_error_unexpected
}

private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

private const val HOURS_SHOWN = 24
private const val SECONDS_IN_HOUR = 3_600L
