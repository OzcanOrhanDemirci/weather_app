package com.ozcanorhandemirci.hava.core.sky

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.ozcanorhandemirci.hava.core.designsystem.theme.ColorContrast
import com.ozcanorhandemirci.hava.core.designsystem.theme.SkyPalette
import com.ozcanorhandemirci.hava.core.model.CloudCover
import com.ozcanorhandemirci.hava.core.model.Intensity
import com.ozcanorhandemirci.hava.core.model.Precipitation
import com.ozcanorhandemirci.hava.core.model.WeatherKind

/**
 * Turns conditions into the colors of a sky.
 *
 * Two inputs decide a sky, in this order. The height of the sun sets the light,
 * because the difference between noon and midnight is larger than the
 * difference between any two kinds of weather. The weather then acts on that
 * light: cloud drains it, rain darkens it, snow raises it, fog collapses the
 * distance between the top of the sky and the bottom.
 *
 * The result is a continuum rather than a set of named themes. There is no
 * moment at which the sky switches from one preset to another, because there is
 * no preset to switch to.
 */
object SkyPaletteFactory {

    /** Below this height the sun has stopped lighting the sky. */
    private const val CIVIL_TWILIGHT_DEGREES = -6.0

    fun create(conditions: SkyConditions): SkyPalette {
        val sun = SolarGeometry.positionAt(
            instant = conditions.instant,
            latitude = conditions.coordinates.latitude,
            longitude = conditions.coordinates.longitude,
        )

        val light = lightAt(sun.altitudeDegrees.toFloat())
        val weathered = light.shapedBy(conditions.kind, daylightAt(sun.altitudeDegrees))
        val isNight = sun.altitudeDegrees < CIVIL_TWILIGHT_DEGREES

        return weathered.toPalette(isNight)
    }

    /**
     * The colors of the light itself, before any weather.
     *
     * Interpolated between heights that matter: astronomical night, the blue
     * hour, the horizon crossing, the golden hour, and full day.
     */
    private fun lightAt(altitudeDegrees: Float): SkyLight {
        val stops = DaylightStops
        if (altitudeDegrees <= stops.first().altitude) return stops.first().light
        if (altitudeDegrees >= stops.last().altitude) return stops.last().light

        val upperIndex = stops.indexOfFirst { it.altitude >= altitudeDegrees }
        val lower = stops[upperIndex - 1]
        val upper = stops[upperIndex]
        val span = upper.altitude - lower.altitude
        val progress = if (span == 0f) 0f else (altitudeDegrees - lower.altitude) / span

        return lower.light.blendedWith(upper.light, progress)
    }

    /**
     * How much light the sun is still putting into the sky, from none once it is
     * below civil twilight to full once it is clear of the horizon.
     */
    private fun daylightAt(altitudeDegrees: Double): Float =
        ((altitudeDegrees - CIVIL_TWILIGHT_DEGREES) / (DAYLIGHT_FULL_DEGREES - CIVIL_TWILIGHT_DEGREES))
            .coerceIn(0.0, 1.0)
            .toFloat()

    private fun SkyLight.shapedBy(kind: WeatherKind, daylight: Float): SkyLight {
        var result = this

        val cloudiness = when (kind.cloudCover) {
            CloudCover.CLEAR -> 0f
            CloudCover.FEW -> 0.10f
            CloudCover.SCATTERED -> 0.26f
            CloudCover.OVERCAST -> 0.62f
        }
        if (cloudiness > 0f) result = result.drainedTowardsGrey(cloudiness)

        val severity = when (kind.intensity) {
            Intensity.NONE -> 0f
            Intensity.LIGHT -> 0.4f
            Intensity.MODERATE -> 0.7f
            Intensity.HEAVY -> 1f
        }

        result = when (kind.precipitation) {
            Precipitation.NONE -> result
            Precipitation.RAIN -> result.darkenedBy(0.32f * severity)
            Precipitation.SLEET -> result.darkenedBy(0.16f * severity)
            // Snow brightens a sky rather than darkening it: the cloud is low
            // and the ground throws light back up into it. That only works while
            // there is light to throw, so the effect follows the sun down.
            Precipitation.SNOW -> result
                .liftedBy(0.16f * severity * (RESIDUAL_NIGHT_GLOW + (1f - RESIDUAL_NIGHT_GLOW) * daylight))
                .cooledBy(0.18f * (RESIDUAL_NIGHT_GLOW + (1f - RESIDUAL_NIGHT_GLOW) * daylight))
        }

        if (kind.hasLightning) result = result.darkenedBy(0.30f).tintedWith(StormTint, 0.22f)
        if (kind.reducesVisibility) result = result.flattenedBy(0.72f)

        return result
    }

    private fun SkyLight.toPalette(isNight: Boolean): SkyPalette {
        val content = Color(0xFFF4F7FF)

        // The pane that carries content is darkened just enough to reach the
        // contrast requirement over this particular sky, and no further, so the
        // weather stays visible through it.
        //
        // The veil is always dark, never light. Content is near white, so a
        // light veil moves the pane towards the text rather than away from it:
        // raising its opacity would lower the contrast instead of raising it,
        // and the search for a sufficient opacity would never terminate
        // successfully. It keeps the hue of the sky it covers so that a pane on
        // a winter dusk is not the same color as one on a summer noon.
        val veil = lerp(Color.Black, haze, VEIL_HUE_RETAINED)
        val backdropForContent = lerp(zenith, horizon, CONTENT_SITS_AT)
        val veilAlpha = ColorContrast.minimumVeilAlpha(
            veil = veil,
            background = backdropForContent,
            content = content,
            minimum = MINIMUM_VISIBLE_VEIL,
        )

        return SkyPalette(
            zenith = zenith,
            horizon = horizon,
            haze = haze,
            luminary = luminary,
            luminaryGlow = glow,
            cloud = cloud,
            cloudShade = cloudShade,
            accent = accent,
            content = content,
            contentMuted = content.copy(alpha = 0.72f),
            glass = veil.copy(alpha = veilAlpha),
            glassEdge = Color.White.copy(alpha = if (isNight) 0.14f else 0.26f),
            isNight = isNight,
        )
    }

    /** Above this height the sun is lighting the sky at full strength. */
    private const val DAYLIGHT_FULL_DEGREES = 6.0

    /** Share of the snow brightening that survives after dark, from ground light. */
    private const val RESIDUAL_NIGHT_GLOW = 0.22f

    /** Height on the screen where a pane of content typically sits. */
    private const val CONTENT_SITS_AT = 0.45f

    /** How much of the sky color survives in the veil that darkens it. */
    private const val VEIL_HUE_RETAINED = 0.22f

    /**
     * On a dark sky the contrast requirement is met almost immediately, but a
     * pane that thin would be invisible. This keeps it perceptible; on those
     * skies it is the hairline edge that does most of the work of separating it
     * from the background.
     */
    private const val MINIMUM_VISIBLE_VEIL = 0.12f

    private val StormTint = Color(0xFF473C6E)
}

/**
 * The colors of one sky before they are published as roles.
 *
 * Kept separate from [SkyPalette] so the blending arithmetic has somewhere to
 * live that does not leak into the design system contract.
 */
internal data class SkyLight(
    val zenith: Color,
    val haze: Color,
    val horizon: Color,
    val luminary: Color,
    val glow: Color,
    val cloud: Color,
    val cloudShade: Color,
    val accent: Color,
) {

    fun blendedWith(other: SkyLight, fraction: Float) = SkyLight(
        zenith = lerp(zenith, other.zenith, fraction),
        haze = lerp(haze, other.haze, fraction),
        horizon = lerp(horizon, other.horizon, fraction),
        luminary = lerp(luminary, other.luminary, fraction),
        glow = lerp(glow, other.glow, fraction),
        cloud = lerp(cloud, other.cloud, fraction),
        cloudShade = lerp(cloudShade, other.cloudShade, fraction),
        accent = lerp(accent, other.accent, fraction),
    )

    /**
     * Mixes the sky towards a grey of its own brightness, which is what cloud
     * does: it removes color without deciding whether the result is light or
     * dark. An overcast noon stays bright; an overcast midnight stays black.
     */
    fun drainedTowardsGrey(amount: Float) = mapSky { color ->
        lerp(color, color.greyOfSameBrightness(), amount)
    }

    fun darkenedBy(amount: Float) = mapSky { it.mixedWith(Color.Black, amount) }

    fun liftedBy(amount: Float) = mapSky { it.mixedWith(Color.White, amount) }

    fun cooledBy(amount: Float) = mapSky { it.mixedWith(WinterBlue, amount) }

    fun tintedWith(tint: Color, amount: Float) = mapSky { it.mixedWith(tint, amount) }

    /**
     * Pulls the top and the bottom of the sky towards the middle. Fog removes
     * depth before it removes light: the horizon stops being further away than
     * the zenith.
     */
    fun flattenedBy(amount: Float): SkyLight {
        val middle = lerp(lerp(zenith, horizon, 0.5f), Color.White, 0.22f * amount)
        return copy(
            zenith = lerp(zenith, middle, amount),
            haze = lerp(haze, middle, amount),
            horizon = lerp(horizon, middle, amount),
            glow = glow.copy(alpha = glow.alpha * (1f - 0.5f * amount)),
        )
    }

    private inline fun mapSky(transform: (Color) -> Color) = copy(
        zenith = transform(zenith),
        haze = transform(haze),
        horizon = transform(horizon),
        cloud = transform(cloud),
        cloudShade = transform(cloudShade),
    )

    private companion object {
        val WinterBlue = Color(0xFFBFD4E8)
    }
}

private fun Color.mixedWith(other: Color, amount: Float): Color = lerp(this, other, amount)

private fun Color.greyOfSameBrightness(): Color {
    val luminance = ColorContrast.relativeLuminance(this).toFloat()
    // Undo the transfer function so the grey reads as the same brightness
    // rather than the same numeric channel value.
    val channel = if (luminance <= 0.0031308f) {
        luminance * 12.92f
    } else {
        1.055f * Math.pow(luminance.toDouble(), 1.0 / 2.4).toFloat() - 0.055f
    }
    val value = channel.coerceIn(0f, 1f)
    // Cloud is never perfectly neutral; it keeps a trace of the sky behind it.
    return Color(red = value, green = value, blue = (value * 1.03f).coerceAtMost(1f))
}

/**
 * Skies at the heights that matter, from astronomical night to full day.
 *
 * Between these the light is interpolated, which is why a sunrise arrives as a
 * movement rather than as a change of theme.
 */
private val DaylightStops = listOf(
    DaylightStop(
        altitude = -18f,
        light = SkyLight(
            zenith = Color(0xFF04060F),
            haze = Color(0xFF0B1226),
            horizon = Color(0xFF131C38),
            luminary = Color(0xFFEFF3FF),
            glow = Color(0x332C3C6E),
            cloud = Color(0xFF232C4C),
            cloudShade = Color(0xFF141A31),
            accent = Color(0xFF8FB8FF),
        ),
    ),
    DaylightStop(
        altitude = -12f,
        light = SkyLight(
            zenith = Color(0xFF070D22),
            haze = Color(0xFF16224B),
            horizon = Color(0xFF273A70),
            luminary = Color(0xFFEFF3FF),
            glow = Color(0x3D3A54A0),
            cloud = Color(0xFF2E3960),
            cloudShade = Color(0xFF1B2342),
            accent = Color(0xFF9CC0FF),
        ),
    ),
    DaylightStop(
        altitude = -6f,
        light = SkyLight(
            zenith = Color(0xFF14204F),
            haze = Color(0xFF41417C),
            horizon = Color(0xFF9A5B60),
            luminary = Color(0xFFFFD9B0),
            glow = Color(0x4DB4724F),
            cloud = Color(0xFF4A4670),
            cloudShade = Color(0xFF2B2B4E),
            accent = Color(0xFFE2A0A8),
        ),
    ),
    DaylightStop(
        altitude = 0f,
        light = SkyLight(
            zenith = Color(0xFF2A4A8C),
            haze = Color(0xFFC0784F),
            horizon = Color(0xFFF2A05C),
            luminary = Color(0xFFFFE0A8),
            glow = Color(0x66F09A4A),
            cloud = Color(0xFFE2A882),
            cloudShade = Color(0xFF8A5F5A),
            accent = Color(0xFFFFB067),
        ),
    ),
    DaylightStop(
        altitude = 6f,
        light = SkyLight(
            zenith = Color(0xFF2F6BAC),
            haze = Color(0xFF9EB8D8),
            horizon = Color(0xFFF3C889),
            luminary = Color(0xFFFFF0CC),
            glow = Color(0x66FFD285),
            cloud = Color(0xFFF7E4D2),
            cloudShade = Color(0xFFB79C9A),
            accent = Color(0xFFFFC857),
        ),
    ),
    DaylightStop(
        altitude = 20f,
        light = SkyLight(
            zenith = Color(0xFF1E63B4),
            haze = Color(0xFF7FB2E0),
            horizon = Color(0xFFC3DEF4),
            luminary = Color(0xFFFFF8E2),
            glow = Color(0x59FFE9A8),
            cloud = Color(0xFFFAFCFE),
            cloudShade = Color(0xFFBFD2E4),
            accent = Color(0xFFFFC857),
        ),
    ),
    DaylightStop(
        altitude = 55f,
        light = SkyLight(
            zenith = Color(0xFF1466C6),
            haze = Color(0xFF6FB2EA),
            horizon = Color(0xFFD2E9FB),
            luminary = Color(0xFFFFFDF2),
            glow = Color(0x59FFF3C4),
            cloud = Color(0xFFFFFFFF),
            cloudShade = Color(0xFFC6D9E9),
            accent = Color(0xFFFFC857),
        ),
    ),
)

private data class DaylightStop(val altitude: Float, val light: SkyLight)
