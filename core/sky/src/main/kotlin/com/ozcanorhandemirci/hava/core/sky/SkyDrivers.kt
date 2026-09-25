package com.ozcanorhandemirci.hava.core.sky

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import com.ozcanorhandemirci.hava.core.model.CloudCover
import com.ozcanorhandemirci.hava.core.model.Intensity
import com.ozcanorhandemirci.hava.core.model.Precipitation

/**
 * Everything about a sky that can be moved through rather than switched.
 *
 * The palette was animated from the start, which was enough to notice that the
 * rest was not: cloud appeared, rain started and stars arrived all at once, on
 * the frame the conditions changed, while the colors were still travelling.
 * Half a transition looks worse than none.
 *
 * Rain and snow are carried as two separate amounts rather than as a type and a
 * strength. A type cannot be interpolated, so a sky going from rain to snow
 * would have had to pick a frame to change on. As two amounts, the rain thins
 * out while the snow thickens, and for a moment both are falling, which is what
 * actually happens outside.
 */
@Immutable
data class SkyDrivers(
    /** How much cloud there is, from nothing to overcast. */
    val cloudiness: Float,
    /** How far night has fallen, which is what brings the stars out. */
    val nightfall: Float,
    val rain: Float,
    val snow: Float,
    val fog: Float,
    val lightning: Float,
    /** Where the sun or moon sits, as a fraction of the drawing area. */
    val luminaryX: Float,
    val luminaryY: Float,
    /** How much of the luminary is hidden by what is in front of it. */
    val luminaryHidden: Float,
)

/**
 * Animates every driver towards the sky that is now current.
 *
 * All of them share one specification, so nothing arrives before anything else.
 */
@Composable
internal fun rememberSkyDrivers(
    state: SkyState,
    transition: SkyTransition,
): SkyDrivers {
    val spec = transition.spec<Float>()
    val kind = state.conditions.kind
    val severity = kind.intensity.asAmount()

    val targetCloudiness = kind.cloudCover.asAmount()
    val targetFog = if (kind.reducesVisibility) FOG_STRENGTH else 0f
    val targetHidden = maxOf(targetCloudiness, targetFog)

    val luminary = state.visibleBody.toScreenFraction(state.conditions.coordinates.latitude)
    val visible = state.visibleBody.altitudeDegrees > BODY_VISIBLE_ABOVE

    val cloudiness by animateFloatAsState(targetCloudiness, spec, label = "cloudiness")
    val nightfall by animateFloatAsState(state.nightfall, spec, label = "nightfall")
    val rain by animateFloatAsState(
        targetValue = if (kind.precipitation.isLiquid) severity else 0f,
        animationSpec = spec,
        label = "rain",
    )
    val snow by animateFloatAsState(
        targetValue = if (kind.precipitation == Precipitation.SNOW) severity else 0f,
        animationSpec = spec,
        label = "snow",
    )
    val fog by animateFloatAsState(targetFog, spec, label = "fog")
    val lightning by animateFloatAsState(
        targetValue = if (kind.hasLightning) 1f else 0f,
        animationSpec = spec,
        label = "lightning",
    )
    val luminaryX by animateFloatAsState(luminary.first, spec, label = "luminaryX")
    val luminaryY by animateFloatAsState(luminary.second, spec, label = "luminaryY")
    val hidden by animateFloatAsState(
        targetValue = if (visible) targetHidden else 1f,
        animationSpec = spec,
        label = "luminaryHidden",
    )

    return SkyDrivers(
        cloudiness = cloudiness,
        nightfall = nightfall,
        rain = rain,
        snow = snow,
        fog = fog,
        lightning = lightning,
        luminaryX = luminaryX,
        luminaryY = luminaryY,
        luminaryHidden = hidden,
    )
}

private val Precipitation.isLiquid: Boolean
    get() = this == Precipitation.RAIN || this == Precipitation.SLEET

private fun CloudCover.asAmount(): Float = when (this) {
    CloudCover.CLEAR -> 0f
    CloudCover.FEW -> 0.22f
    CloudCover.SCATTERED -> 0.5f
    CloudCover.OVERCAST -> 1f
}

private fun Intensity.asAmount(): Float = when (this) {
    Intensity.NONE -> 0f
    Intensity.LIGHT -> 0.42f
    Intensity.MODERATE -> 0.72f
    Intensity.HEAVY -> 1f
}

private const val FOG_STRENGTH = 0.85f

/** Below this the body has set and there is nothing left to draw. */
private const val BODY_VISIBLE_ABOVE = -6.0
