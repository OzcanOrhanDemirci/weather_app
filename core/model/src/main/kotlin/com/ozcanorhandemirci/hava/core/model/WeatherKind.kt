package com.ozcanorhandemirci.hava.core.model

/** The phenomenon reported for a point in time. */
enum class WeatherCondition {
    CLEAR,
    MAINLY_CLEAR,
    PARTLY_CLOUDY,
    OVERCAST,
    FOG,
    DRIZZLE,
    FREEZING_DRIZZLE,
    RAIN,
    FREEZING_RAIN,
    RAIN_SHOWER,
    SNOW,
    SNOW_GRAINS,
    SNOW_SHOWER,
    THUNDERSTORM,
    THUNDERSTORM_WITH_HAIL,
    UNKNOWN,
}

/** How strongly the phenomenon is reported. */
enum class Intensity {
    NONE,
    LIGHT,
    MODERATE,
    HEAVY,
}

/** What is falling from the sky, if anything. */
enum class Precipitation {
    NONE,
    RAIN,
    SNOW,
    SLEET,
}

/** How much of the sky is covered, expressed in the steps the forecast reports. */
enum class CloudCover {
    CLEAR,
    FEW,
    SCATTERED,
    OVERCAST,
}

/**
 * A World Meteorological Organization interpretation code, as published by
 * Open-Meteo, resolved into the terms the rest of the application reasons about.
 *
 * The derived properties exist so that neither the presentation layer nor the
 * backdrop has to know the numeric codes.
 */
data class WeatherKind(
    val condition: WeatherCondition,
    val intensity: Intensity,
) {

    val precipitation: Precipitation
        get() = when (condition) {
            WeatherCondition.DRIZZLE,
            WeatherCondition.RAIN,
            WeatherCondition.RAIN_SHOWER,
            WeatherCondition.THUNDERSTORM,
            WeatherCondition.THUNDERSTORM_WITH_HAIL,
            -> Precipitation.RAIN

            WeatherCondition.SNOW,
            WeatherCondition.SNOW_GRAINS,
            WeatherCondition.SNOW_SHOWER,
            -> Precipitation.SNOW

            WeatherCondition.FREEZING_DRIZZLE,
            WeatherCondition.FREEZING_RAIN,
            -> Precipitation.SLEET

            else -> Precipitation.NONE
        }

    val cloudCover: CloudCover
        get() = when (condition) {
            WeatherCondition.CLEAR -> CloudCover.CLEAR
            WeatherCondition.MAINLY_CLEAR -> CloudCover.FEW
            WeatherCondition.PARTLY_CLOUDY -> CloudCover.SCATTERED
            WeatherCondition.UNKNOWN -> CloudCover.FEW
            else -> CloudCover.OVERCAST
        }

    val hasLightning: Boolean
        get() = condition == WeatherCondition.THUNDERSTORM ||
            condition == WeatherCondition.THUNDERSTORM_WITH_HAIL

    val reducesVisibility: Boolean
        get() = condition == WeatherCondition.FOG

    companion object {
        /** Resolves a WMO code. Unknown codes degrade to [WeatherCondition.UNKNOWN] rather than failing. */
        fun fromWmoCode(code: Int): WeatherKind = when (code) {
            0 -> WeatherKind(WeatherCondition.CLEAR, Intensity.NONE)
            1 -> WeatherKind(WeatherCondition.MAINLY_CLEAR, Intensity.NONE)
            2 -> WeatherKind(WeatherCondition.PARTLY_CLOUDY, Intensity.NONE)
            3 -> WeatherKind(WeatherCondition.OVERCAST, Intensity.NONE)

            45 -> WeatherKind(WeatherCondition.FOG, Intensity.MODERATE)
            48 -> WeatherKind(WeatherCondition.FOG, Intensity.HEAVY)

            51 -> WeatherKind(WeatherCondition.DRIZZLE, Intensity.LIGHT)
            53 -> WeatherKind(WeatherCondition.DRIZZLE, Intensity.MODERATE)
            55 -> WeatherKind(WeatherCondition.DRIZZLE, Intensity.HEAVY)
            56 -> WeatherKind(WeatherCondition.FREEZING_DRIZZLE, Intensity.LIGHT)
            57 -> WeatherKind(WeatherCondition.FREEZING_DRIZZLE, Intensity.HEAVY)

            61 -> WeatherKind(WeatherCondition.RAIN, Intensity.LIGHT)
            63 -> WeatherKind(WeatherCondition.RAIN, Intensity.MODERATE)
            65 -> WeatherKind(WeatherCondition.RAIN, Intensity.HEAVY)
            66 -> WeatherKind(WeatherCondition.FREEZING_RAIN, Intensity.LIGHT)
            67 -> WeatherKind(WeatherCondition.FREEZING_RAIN, Intensity.HEAVY)

            71 -> WeatherKind(WeatherCondition.SNOW, Intensity.LIGHT)
            73 -> WeatherKind(WeatherCondition.SNOW, Intensity.MODERATE)
            75 -> WeatherKind(WeatherCondition.SNOW, Intensity.HEAVY)
            77 -> WeatherKind(WeatherCondition.SNOW_GRAINS, Intensity.LIGHT)

            80 -> WeatherKind(WeatherCondition.RAIN_SHOWER, Intensity.LIGHT)
            81 -> WeatherKind(WeatherCondition.RAIN_SHOWER, Intensity.MODERATE)
            82 -> WeatherKind(WeatherCondition.RAIN_SHOWER, Intensity.HEAVY)
            85 -> WeatherKind(WeatherCondition.SNOW_SHOWER, Intensity.LIGHT)
            86 -> WeatherKind(WeatherCondition.SNOW_SHOWER, Intensity.HEAVY)

            95 -> WeatherKind(WeatherCondition.THUNDERSTORM, Intensity.MODERATE)
            96 -> WeatherKind(WeatherCondition.THUNDERSTORM_WITH_HAIL, Intensity.LIGHT)
            99 -> WeatherKind(WeatherCondition.THUNDERSTORM_WITH_HAIL, Intensity.HEAVY)

            else -> WeatherKind(WeatherCondition.UNKNOWN, Intensity.NONE)
        }
    }
}
