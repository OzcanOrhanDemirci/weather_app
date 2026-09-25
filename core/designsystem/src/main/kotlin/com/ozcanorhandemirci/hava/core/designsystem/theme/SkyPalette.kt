package com.ozcanorhandemirci.hava.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The colors of one sky.
 *
 * The application carries no fixed palette. Every screen is drawn over a
 * backdrop derived from the weather and the local time of the place on show,
 * and this type is the contract between that backdrop and everything painted
 * on top of it. A component asks for the color of a role; it never chooses a
 * literal color of its own.
 */
@Immutable
data class SkyPalette(
    /** Color at the top of the dome. */
    val zenith: Color,
    /** Color where the dome meets the ground. */
    val horizon: Color,
    /** Atmospheric haze mixed in close to the horizon. */
    val haze: Color,
    /** The sun or the moon. */
    val luminary: Color,
    /** The glow cast around the luminary. */
    val luminaryGlow: Color,
    val cloud: Color,
    val cloudShade: Color,
    /** Accent for interactive elements and highlights. */
    val accent: Color,
    /** Primary color for content placed over this sky. */
    val content: Color,
    /** Color for content that must recede. */
    val contentMuted: Color,
    /** Fill of the translucent surfaces that carry content. */
    val glass: Color,
    /** Hairline separating a translucent surface from the sky behind it. */
    val glassEdge: Color,
    /** True when the sky reads as dark, which flips shadows and icon treatment. */
    val isNight: Boolean,
) {

    companion object {

        /**
         * Shown before a real sky is known, and used by previews. A muted
         * daylight blue rather than a grey, so an unresolved state never looks
         * like a failure.
         */
        val Placeholder = SkyPalette(
            zenith = Color(0xFF2B6CB8),
            horizon = Color(0xFF8FC0E8),
            haze = Color(0xFFCFE3F5),
            luminary = Color(0xFFFFF4D6),
            luminaryGlow = Color(0x66FFE9A8),
            cloud = Color(0xFFF4F8FC),
            cloudShade = Color(0xFFC2D3E4),
            accent = Color(0xFFFFC857),
            content = Color(0xFFFFFFFF),
            contentMuted = Color(0xCCFFFFFF),
            glass = Color(0x33FFFFFF),
            glassEdge = Color(0x40FFFFFF),
            isNight = false,
        )

        /** Night counterpart of [Placeholder], used by previews of dark states. */
        val PlaceholderNight = SkyPalette(
            zenith = Color(0xFF07091A),
            horizon = Color(0xFF1B2242),
            haze = Color(0xFF2E3A63),
            luminary = Color(0xFFE8ECFF),
            luminaryGlow = Color(0x4DBFD0FF),
            cloud = Color(0xFF3A4468),
            cloudShade = Color(0xFF232A47),
            accent = Color(0xFF8FB8FF),
            content = Color(0xFFF2F5FF),
            contentMuted = Color(0xB3D6DEF5),
            glass = Color(0x1FFFFFFF),
            glassEdge = Color(0x2EFFFFFF),
            isNight = true,
        )
    }
}

/**
 * The sky the current subtree is painted over.
 *
 * Static because it changes as a whole when the place or the hour changes, and
 * every read should recompose when it does.
 */
val LocalSkyPalette = staticCompositionLocalOf { SkyPalette.Placeholder }
