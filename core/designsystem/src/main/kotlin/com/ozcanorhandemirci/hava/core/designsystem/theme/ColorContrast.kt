package com.ozcanorhandemirci.hava.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Contrast arithmetic from the Web Content Accessibility Guidelines.
 *
 * The palette of this application is generated rather than chosen, so contrast
 * cannot be checked once by eye and signed off. It has to be computed, which is
 * what these functions exist for.
 */
object ColorContrast {

    /** The guideline minimum for body text. */
    const val MINIMUM_FOR_TEXT = 4.5

    /** The guideline minimum for text at 24sp and above, or bold at 19sp. */
    const val MINIMUM_FOR_LARGE_TEXT = 3.0

    /**
     * Relative luminance, with the transfer function the guidelines specify
     * rather than a plain average of the channels.
     */
    fun relativeLuminance(color: Color): Double {
        fun channel(value: Float): Double {
            val v = value.toDouble()
            return if (v <= 0.03928) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * channel(color.red) +
            0.7152 * channel(color.green) +
            0.0722 * channel(color.blue)
    }

    /** Contrast ratio between two opaque colors, from 1.0 to 21.0. */
    fun ratio(foreground: Color, background: Color): Double {
        val a = relativeLuminance(foreground)
        val b = relativeLuminance(background)
        return (max(a, b) + 0.05) / (min(a, b) + 0.05)
    }

    /** Flattens a translucent color onto an opaque one. */
    fun composite(foreground: Color, background: Color): Color {
        val alpha = foreground.alpha
        return Color(
            red = foreground.red * alpha + background.red * (1f - alpha),
            green = foreground.green * alpha + background.green * (1f - alpha),
            blue = foreground.blue * alpha + background.blue * (1f - alpha),
            alpha = 1f,
        )
    }

    /**
     * The lowest opacity of [veil] over [background] that lets [content] reach
     * [targetRatio].
     *
     * A translucent pane is what lets the weather show through, so the answer
     * should be the least opacity that still works rather than a safe constant
     * that hides the sky on every screen. Search runs from clear to opaque and
     * stops at the first opacity that satisfies the requirement; if none does,
     * the most opaque candidate is returned so the result is always legible.
     */
    fun minimumVeilAlpha(
        veil: Color,
        background: Color,
        content: Color,
        targetRatio: Double = MINIMUM_FOR_TEXT,
        minimum: Float = 0.06f,
        maximum: Float = 0.88f,
        step: Float = 0.02f,
    ): Float {
        var alpha = minimum
        while (alpha < maximum) {
            val flattened = composite(veil.copy(alpha = alpha), background)
            if (ratio(content, flattened) >= targetRatio) return alpha
            alpha += step
        }
        return maximum
    }
}
