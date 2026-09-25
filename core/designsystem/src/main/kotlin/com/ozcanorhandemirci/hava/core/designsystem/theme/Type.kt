package com.ozcanorhandemirci.hava.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.ozcanorhandemirci.hava.core.designsystem.R

/**
 * Inter ships as one variable file with a weight axis and an optical size axis.
 * The optical size axis is what separates text meant to be read at 14sp from a
 * numeral meant to fill half the screen: at display sizes the letterforms
 * tighten and the strokes thin.
 *
 * Two families are derived from the single file so a style can select the
 * drawing that suits its size rather than scale one drawing up.
 */
@OptIn(ExperimentalTextApi::class)
private fun interFont(weight: Int, opticalSize: TextUnit): Font = Font(
    resId = R.font.inter_variable,
    weight = FontWeight(weight),
    style = FontStyle.Normal,
    variationSettings = FontVariation.Settings(
        weight = FontWeight(weight),
        style = FontStyle.Normal,
        FontVariation.opticalSizing(opticalSize),
    ),
)

private fun interFamily(opticalSize: TextUnit) = FontFamily(
    interFont(weight = 200, opticalSize = opticalSize),
    interFont(weight = 300, opticalSize = opticalSize),
    interFont(weight = 400, opticalSize = opticalSize),
    interFont(weight = 500, opticalSize = opticalSize),
    interFont(weight = 600, opticalSize = opticalSize),
    interFont(weight = 700, opticalSize = opticalSize),
)

/** Optical size tuned for running text. */
private val InterText = interFamily(14.sp)

/** Optical size tuned for headlines and numerals. */
private val InterDisplay = interFamily(32.sp)

/** Digits of even width, so a changing value does not shift its neighbours. */
private const val TABULAR_FIGURES = "tnum"

/**
 * Type styles beyond the Material set.
 *
 * Weather is read as numbers, and numbers at this scale need a treatment of
 * their own rather than a body style made larger.
 */
@Immutable
data class HavaTypography(
    /** The temperature that owns the detail screen. */
    val temperatureHero: TextStyle,
    /** The temperature on a city card. */
    val temperatureLarge: TextStyle,
    /** The temperature in a dense row or an hourly strip. */
    val temperatureCompact: TextStyle,
    /** Wind, humidity, pressure and similar figures. */
    val metric: TextStyle,
    /** Small letter spaced label that names a group. */
    val overline: TextStyle,
)

internal val HavaTypographyDefaults = HavaTypography(
    temperatureHero = TextStyle(
        fontFamily = InterDisplay,
        fontWeight = FontWeight.Thin,
        fontSize = 116.sp,
        lineHeight = 116.sp,
        letterSpacing = (-0.045).em,
    ),
    temperatureLarge = TextStyle(
        fontFamily = InterDisplay,
        fontWeight = FontWeight.Light,
        fontSize = 52.sp,
        lineHeight = 54.sp,
        letterSpacing = (-0.035).em,
    ),
    temperatureCompact = TextStyle(
        fontFamily = InterDisplay,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.01).em,
        fontFeatureSettings = TABULAR_FIGURES,
    ),
    metric = TextStyle(
        fontFamily = InterText,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        fontFeatureSettings = TABULAR_FIGURES,
    ),
    overline = TextStyle(
        fontFamily = InterText,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.12.em,
    ),
)

internal val HavaMaterialTypography: Typography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = InterDisplay, fontWeight = FontWeight.Light, letterSpacing = (-0.03).em),
        displayMedium = displayMedium.copy(fontFamily = InterDisplay, fontWeight = FontWeight.Light, letterSpacing = (-0.025).em),
        displaySmall = displaySmall.copy(fontFamily = InterDisplay, fontWeight = FontWeight.Normal, letterSpacing = (-0.02).em),
        headlineLarge = headlineLarge.copy(fontFamily = InterDisplay, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.02).em),
        headlineMedium = headlineMedium.copy(fontFamily = InterDisplay, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.015).em),
        headlineSmall = headlineSmall.copy(fontFamily = InterDisplay, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.01).em),
        titleLarge = titleLarge.copy(fontFamily = InterDisplay, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.01).em),
        titleMedium = titleMedium.copy(fontFamily = InterText, fontWeight = FontWeight.SemiBold),
        titleSmall = titleSmall.copy(fontFamily = InterText, fontWeight = FontWeight.SemiBold),
        bodyLarge = bodyLarge.copy(fontFamily = InterText),
        bodyMedium = bodyMedium.copy(fontFamily = InterText),
        bodySmall = bodySmall.copy(fontFamily = InterText),
        labelLarge = labelLarge.copy(fontFamily = InterText, fontWeight = FontWeight.Medium),
        labelMedium = labelMedium.copy(fontFamily = InterText, fontWeight = FontWeight.Medium),
        labelSmall = labelSmall.copy(fontFamily = InterText, fontWeight = FontWeight.Medium),
    )
}

val LocalHavaTypography = staticCompositionLocalOf { HavaTypographyDefaults }
