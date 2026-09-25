package com.ozcanorhandemirci.hava

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaSpacing
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaTheme
import com.ozcanorhandemirci.hava.core.model.Coordinates
import com.ozcanorhandemirci.hava.core.model.WeatherKind
import com.ozcanorhandemirci.hava.core.sky.SkyConditions
import com.ozcanorhandemirci.hava.core.sky.SkyScene
import java.time.Instant

/**
 * Root of the interface.
 *
 * The backdrop is drawn here and the screens are placed over it, so the sky is
 * continuous as navigation moves between them. The city list takes the position
 * the name currently occupies.
 *
 * The moment and the place are real; the weather is not yet, because nothing
 * fetches it. Everything the sky derives from time and position, which is the
 * light, the height of the sun, the phase of the moon and the stars, is already
 * correct.
 */
@Composable
fun HavaApp(modifier: Modifier = Modifier) {
    val conditions = remember {
        SkyConditions(
            kind = WeatherKind.fromWmoCode(CLEAR_SKY),
            instant = Instant.now(),
            coordinates = Izmir,
            windSpeedKph = 0.0,
            windDirectionDegrees = 0,
        )
    }

    SkyScene(
        conditions = conditions,
        modifier = modifier.fillMaxSize(),
        contentDescription = stringResource(R.string.sky_backdrop_description),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(HavaSpacing.small),
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.displayLarge,
                    color = HavaTheme.sky.content,
                )
                Text(
                    text = stringResource(R.string.app_tagline),
                    style = MaterialTheme.typography.bodyLarge,
                    color = HavaTheme.sky.contentMuted,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

private val Izmir = Coordinates(latitude = 38.4237, longitude = 27.1428)

private const val CLEAR_SKY = 0
