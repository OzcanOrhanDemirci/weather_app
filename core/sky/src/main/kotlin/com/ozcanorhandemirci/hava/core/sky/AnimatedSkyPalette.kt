package com.ozcanorhandemirci.hava.core.sky

import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.ozcanorhandemirci.hava.core.designsystem.theme.SkyPalette

/**
 * A palette that travels to a new sky instead of jumping to it.
 *
 * Moving between two places changes every color in the interface at once.
 * Swapping them produces a flash; moving them produces the impression of the
 * weather itself changing, which is what the screen is trying to say.
 *
 * Each role is animated on its own, so a sunset that is warm at the horizon
 * while it is still blue overhead arrives the way it does outside: from the
 * bottom up.
 */
@Composable
fun animateSkyPalette(
    target: SkyPalette,
    transition: SkyTransition,
): SkyPalette {
    val spec = transition.spec<Color>()
    val zenith by animateColorAsState(target.zenith, spec, label = "zenith")
    val horizon by animateColorAsState(target.horizon, spec, label = "horizon")
    val haze by animateColorAsState(target.haze, spec, label = "haze")
    val luminary by animateColorAsState(target.luminary, spec, label = "luminary")
    val luminaryGlow by animateColorAsState(target.luminaryGlow, spec, label = "luminaryGlow")
    val cloud by animateColorAsState(target.cloud, spec, label = "cloud")
    val cloudShade by animateColorAsState(target.cloudShade, spec, label = "cloudShade")
    val accent by animateColorAsState(target.accent, spec, label = "accent")
    val content by animateColorAsState(target.content, spec, label = "content")
    val contentMuted by animateColorAsState(target.contentMuted, spec, label = "contentMuted")
    val glass by animateColorAsState(target.glass, spec, label = "glass")
    val glassEdge by animateColorAsState(target.glassEdge, spec, label = "glassEdge")

    return SkyPalette(
        zenith = zenith,
        horizon = horizon,
        haze = haze,
        luminary = luminary,
        luminaryGlow = luminaryGlow,
        cloud = cloud,
        cloudShade = cloudShade,
        accent = accent,
        content = content,
        contentMuted = contentMuted,
        glass = glass,
        glassEdge = glassEdge,
        // Not animated: a sky either reads as dark, and flips the treatment of
        // icons and shadows, or it does not. Half of that state means nothing.
        isNight = target.isNight,
    )
}
