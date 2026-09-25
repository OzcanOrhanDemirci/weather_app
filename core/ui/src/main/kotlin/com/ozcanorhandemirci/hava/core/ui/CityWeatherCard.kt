package com.ozcanorhandemirci.hava.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaSpacing
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaTheme
import com.ozcanorhandemirci.hava.core.sky.SkyConditions
import com.ozcanorhandemirci.hava.core.sky.SkyDetail
import com.ozcanorhandemirci.hava.core.sky.SkyScene

/**
 * One city in a list.
 *
 * The card is the sky of that place at that moment, with its name written on
 * it. There is no weather icon because there is nothing left for one to say:
 * the rain on the card is the rain in the forecast.
 *
 * The whole card is one thing to a screen reader. Read out piece by piece it
 * would be a name, then a number, then a word, with nothing to say they belong
 * together.
 */
@Composable
fun CityWeatherCard(
    summary: CitySummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val conditions = summary.conditions()
    val spoken = summary.spokenDescription()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(CARD_HEIGHT)
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
            .clearAndSetSemantics { contentDescription = spoken },
    ) {
        if (conditions != null) {
            SkyScene(
                conditions = conditions,
                detail = SkyDetail.Miniature,
                modifier = Modifier.fillMaxSize(),
            ) {
                CardFace(summary = summary, modifier = Modifier.fillMaxSize())
            }
        } else {
            // Before a forecast arrives the card shows the place alone rather
            // than a guess at its weather.
            Box(modifier = Modifier.fillMaxSize().background(HavaTheme.sky.glass)) {
                CardFace(summary = summary, modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun CardFace(summary: CitySummary, modifier: Modifier = Modifier) {
    val current = summary.snapshot?.current

    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                0f to Color.Transparent,
                SCRIM_START to Color.Transparent,
                1f to Color.Black.copy(alpha = SCRIM_STRENGTH),
            ),
        ),
        contentAlignment = Alignment.BottomStart,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(HavaSpacing.medium),
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = summary.city.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = current?.kind?.describe() ?: stringResource(R.string.card_awaiting_reading),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = MUTED_ON_SCRIM),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (current != null) {
                Text(
                    text = current.temperature.format(),
                    style = HavaTheme.typography.temperatureLarge,
                    color = Color.White,
                )
            }
        }
    }
}

private fun CitySummary.conditions(): SkyConditions? {
    val current = snapshot?.current ?: return null
    return SkyConditions(
        kind = current.kind,
        instant = current.observedAt,
        coordinates = city.coordinates,
        windSpeedKph = current.wind.speedKph,
        windDirectionDegrees = current.wind.directionDegrees,
    )
}

@Composable
private fun CitySummary.spokenDescription(): String {
    val current = snapshot?.current
        ?: return stringResource(R.string.card_reading_pending_description, city.name)

    return stringResource(
        R.string.card_reading_description,
        city.name,
        current.temperature.format(),
        current.kind.describe(),
    )
}

private val CARD_HEIGHT = 132.dp

/** Where the scrim begins, leaving the upper half of the sky untouched. */
private const val SCRIM_START = 0.38f
private const val SCRIM_STRENGTH = 0.55f
private const val MUTED_ON_SCRIM = 0.82f
