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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaTheme
import com.ozcanorhandemirci.hava.core.designsystem.theme.LocalReducedMotion
import com.ozcanorhandemirci.hava.core.sky.layer.CloudField
import com.ozcanorhandemirci.hava.core.sky.layer.PrecipitationField
import com.ozcanorhandemirci.hava.core.sky.layer.StarField
import com.ozcanorhandemirci.hava.core.sky.layer.drawClouds
import com.ozcanorhandemirci.hava.core.sky.layer.drawFog
import com.ozcanorhandemirci.hava.core.sky.layer.drawLightning
import com.ozcanorhandemirci.hava.core.sky.layer.drawLuminaryGlow
import com.ozcanorhandemirci.hava.core.sky.layer.drawMoon
import com.ozcanorhandemirci.hava.core.sky.layer.drawRain
import com.ozcanorhandemirci.hava.core.sky.layer.drawSkyGradient
import com.ozcanorhandemirci.hava.core.sky.layer.drawSnow
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
 *
 * Nothing about a sky is switched. Every quantity that separates one sky from
 * the next, including how much cloud there is and how hard it is raining, is
 * animated towards its new value, so a change of weather is something the
 * screen moves through rather than cuts to.
 */
@Composable
fun Sky(
    state: SkyState,
    modifier: Modifier = Modifier,
    detail: SkyDetail = SkyDetail.Full,
    transition: SkyTransition = rememberSkyTransition(state.conditions),
) {
    val budget = Budget.of(detail)
    val seed = seedFor(state, detail)

    val stars = remember(seed, budget) { StarField(budget.stars, seed) }
    val clouds = remember(seed, budget) { CloudField(budget.clouds, seed + 1) }
    val precipitation = remember(seed, budget) { PrecipitationField(budget.particles, seed + 2) }

    val animated = !LocalReducedMotion.current
    val seconds by rememberSkyClock(animated)

    val drivers = rememberSkyDrivers(state, transition)
    val palette = HavaTheme.sky
    val wind = state.conditions

    Canvas(modifier = modifier.fillMaxSize()) {
        drawSkyGradient(palette)

        drawStars(
            field = stars,
            palette = palette,
            nightfall = drivers.nightfall,
            obscured = maxOf(drivers.cloudiness, drivers.fog),
            seconds = seconds,
        )

        if (drivers.luminaryHidden < 1f) {
            val centre = Offset(drivers.luminaryX * size.width, drivers.luminaryY * size.height)
            when {
                detail == SkyDetail.Backdrop ->
                    drawLuminaryGlow(centre, palette, drivers.luminaryHidden)

                state.showsSun ->
                    drawSun(centre, palette, state.sun.altitudeDegrees, drivers.luminaryHidden)

                else -> drawMoon(centre, palette, state.moonPhase, drivers.luminaryHidden)
            }
        }

        drawClouds(
            field = clouds,
            palette = palette,
            coverage = drivers.cloudiness,
            seconds = seconds,
            windFactor = 1f + (wind.windSpeedKph / WIND_REFERENCE).toFloat(),
        )

        drawRain(
            field = precipitation,
            palette = palette,
            strength = drivers.rain,
            seconds = seconds,
            windSpeedKph = wind.windSpeedKph,
            windDirectionDegrees = wind.windDirectionDegrees,
        )
        drawSnow(
            field = precipitation,
            palette = palette,
            strength = drivers.snow,
            seconds = seconds,
            windSpeedKph = wind.windSpeedKph,
            windDirectionDegrees = wind.windDirectionDegrees,
        )

        drawFog(palette = palette, amount = drivers.fog, seconds = seconds)

        if (drivers.lightning > 0.01f) {
            drawLightning(
                palette = palette,
                brightness = lightningAt(seconds) * drivers.lightning * detail.lightningStrength,
            )
        }
    }
}

/**
 * A sky with content placed on top of it, themed by that sky.
 *
 * This is how a card uses the backdrop: the palette of the sky becomes the
 * palette of everything inside it, so a label and a temperature follow the
 * weather without being told about it.
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
    val transition = rememberSkyTransition(conditions)

    HavaTheme(palette = animateSkyPalette(state.palette, transition)) {
        Box(modifier = modifier) {
            Sky(
                state = state,
                detail = detail,
                transition = transition,
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
 * Which arrangement of stars and cloud to use.
 *
 * A card gets its own, derived from where it is, so twenty cards are twenty
 * different skies. The backdrop gets a fixed one, because it is a single sky
 * that the reader is standing under: rearranging its stars every time they
 * scroll past a different city would be the one thing in this application that
 * looks like a defect rather than like weather.
 */
private fun seedFor(state: SkyState, detail: SkyDetail): Long = when (detail) {
    SkyDetail.Backdrop -> FIXED_BACKDROP_SEED
    else -> state.conditions.coordinates.let { (it.latitude * 1_000 + it.longitude * 7).toLong() }
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
 * How much of a storm to show.
 *
 * A flash at full strength belongs to a sky with nothing on top of it. Behind
 * content it is turned down until it reads as a storm somewhere in the
 * distance, and on a card in a list it is off: a row that flashes while being
 * scrolled past is not atmosphere, it is a fault.
 */
private val SkyDetail.lightningStrength: Float
    get() = when (this) {
        SkyDetail.Full -> 1f
        SkyDetail.Backdrop -> 0.45f
        SkyDetail.Miniature -> 0f
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
            SkyDetail.Backdrop -> Budget(stars = 120, clouds = 6, particles = 120)
        }
    }
}

private const val MILLIS_PER_SECOND = 1000f
private const val WIND_REFERENCE = 40.0

/** Any constant would do; this one is simply the one that was chosen. */
private const val FIXED_BACKDROP_SEED = 20_260_925L
