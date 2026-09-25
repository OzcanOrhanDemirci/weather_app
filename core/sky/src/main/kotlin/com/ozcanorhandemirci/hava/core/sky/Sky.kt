package com.ozcanorhandemirci.hava.core.sky

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaTheme
import com.ozcanorhandemirci.hava.core.designsystem.theme.LocalReducedMotion
import com.ozcanorhandemirci.hava.core.model.CloudCover
import com.ozcanorhandemirci.hava.core.sky.layer.CloudField
import com.ozcanorhandemirci.hava.core.sky.layer.PrecipitationField
import com.ozcanorhandemirci.hava.core.sky.layer.StarField
import com.ozcanorhandemirci.hava.core.sky.layer.drawClouds
import com.ozcanorhandemirci.hava.core.sky.layer.drawFog
import com.ozcanorhandemirci.hava.core.sky.layer.drawLightning
import com.ozcanorhandemirci.hava.core.sky.layer.drawMoon
import com.ozcanorhandemirci.hava.core.sky.layer.drawPrecipitation
import com.ozcanorhandemirci.hava.core.sky.layer.drawSkyGradient
import com.ozcanorhandemirci.hava.core.sky.layer.drawStars
import com.ozcanorhandemirci.hava.core.sky.layer.drawSun
import com.ozcanorhandemirci.hava.core.sky.layer.lightningAt

/**
 * The sky for one place at one moment.
 *
 * Layers are drawn from the furthest away to the nearest: the light itself,
 * then the stars behind everything, the sun or the moon, the cloud in front of
 * them, whatever is falling, and finally the fog that stands between the viewer
 * and all of it.
 */
@Composable
fun Sky(
    state: SkyState,
    modifier: Modifier = Modifier,
    detail: SkyDetail = SkyDetail.Full,
) {
    val budget = Budget.of(detail)
    val seed = state.conditions.coordinates.let { (it.latitude * 1_000 + it.longitude * 7).toLong() }

    val stars = remember(seed, budget) { StarField(budget.stars, seed) }
    val clouds = remember(seed, budget) { CloudField(budget.clouds, seed + 1) }
    val precipitation = remember(seed, budget) { PrecipitationField(budget.particles, seed + 2) }

    val animated = !LocalReducedMotion.current
    val seconds by rememberSkyClock(animated)

    val kind = state.conditions.kind
    val palette = state.palette

    val coverage = when (kind.cloudCover) {
        CloudCover.CLEAR -> 0f
        CloudCover.FEW -> 0.22f
        CloudCover.SCATTERED -> 0.5f
        CloudCover.OVERCAST -> 1f
    }
    val fog = if (kind.reducesVisibility) FOG_STRENGTH else 0f

    Canvas(modifier = modifier.fillMaxSize()) {
        drawSkyGradient(palette)

        drawStars(
            field = stars,
            palette = palette,
            nightfall = state.nightfall,
            obscured = maxOf(coverage, fog),
            seconds = seconds,
        )

        val body = state.visibleBody
        if (body.altitudeDegrees > BODY_VISIBLE_ABOVE) {
            val centre = body.toScreenPosition(state.conditions.coordinates.latitude, size)
            if (state.showsSun) {
                drawSun(
                    centre = centre,
                    palette = palette,
                    altitudeDegrees = body.altitudeDegrees,
                    obscured = maxOf(coverage, fog),
                )
            } else {
                drawMoon(
                    centre = centre,
                    palette = palette,
                    phase = state.moonPhase,
                    obscured = maxOf(coverage, fog),
                )
            }
        }

        drawClouds(
            field = clouds,
            palette = palette,
            coverage = coverage,
            seconds = seconds,
            windFactor = 1f + (state.conditions.windSpeedKph / WIND_REFERENCE).toFloat(),
        )

        drawPrecipitation(
            field = precipitation,
            palette = palette,
            precipitation = kind.precipitation,
            intensity = kind.intensity,
            seconds = seconds,
            windSpeedKph = state.conditions.windSpeedKph,
            windDirectionDegrees = state.conditions.windDirectionDegrees,
        )

        drawFog(palette = palette, amount = fog, seconds = seconds)

        if (kind.hasLightning && detail == SkyDetail.Full) {
            drawLightning(palette = palette, brightness = lightningAt(seconds))
        }
    }
}

/**
 * A sky with content placed on top of it, themed by that sky.
 *
 * This is how a screen uses the backdrop: the palette of the sky becomes the
 * palette of everything inside it, so a card, a label and a divider all follow
 * the weather without being told about it.
 */
@Composable
fun SkyScene(
    conditions: SkyConditions,
    modifier: Modifier = Modifier,
    detail: SkyDetail = SkyDetail.Full,
    contentDescription: String? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val state = rememberSkyState(conditions)

    HavaTheme(palette = state.palette) {
        Box(modifier = modifier) {
            Sky(
                state = state,
                detail = detail,
                modifier = Modifier
                    .matchParentSize()
                    .then(
                        if (contentDescription != null) {
                            Modifier.semantics { this.contentDescription = contentDescription }
                        } else {
                            Modifier
                        },
                    ),
            )
            content()
        }
    }
}

/**
 * Elapsed seconds since the sky appeared.
 *
 * Driven by the animation frame clock rather than by a wall clock, so it pauses
 * with the window and stops entirely when the device asks for reduced motion.
 * Every layer is a function of this value, which is why a stopped clock leaves a
 * correct still image rather than an empty one.
 */
@Composable
private fun rememberSkyClock(animated: Boolean): State<Float> {
    val seconds = remember { mutableFloatStateOf(0f) }

    LaunchedEffect(animated) {
        if (!animated) return@LaunchedEffect

        val start = withInfiniteAnimationFrameMillis { it }
        while (true) {
            withInfiniteAnimationFrameMillis { millis ->
                seconds.floatValue = (millis - start) / MILLIS_PER_SECOND
            }
        }
    }

    return seconds
}

/**
 * How many particles a sky may spend.
 *
 * A screen sized backdrop is drawn once; a card sized one is drawn for every
 * row on screen at the same time. They cannot afford the same sky.
 */
private data class Budget(val stars: Int, val clouds: Int, val particles: Int) {
    companion object {
        fun of(detail: SkyDetail): Budget = when (detail) {
            SkyDetail.Full -> Budget(stars = 160, clouds = 8, particles = 180)
            SkyDetail.Miniature -> Budget(stars = 40, clouds = 4, particles = 60)
        }
    }
}

private const val MILLIS_PER_SECOND = 1000f
private const val BODY_VISIBLE_ABOVE = -6.0
private const val WIND_REFERENCE = 40.0
private const val FOG_STRENGTH = 0.85f
