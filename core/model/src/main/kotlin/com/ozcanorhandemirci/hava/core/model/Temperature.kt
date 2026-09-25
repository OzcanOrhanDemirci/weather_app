package com.ozcanorhandemirci.hava.core.model

import kotlin.math.roundToInt

/** The unit a temperature is presented in. */
enum class TemperatureUnit {
    CELSIUS,
    FAHRENHEIT,
}

/**
 * A temperature held in a single canonical unit.
 *
 * The forecast is requested in Celsius and converted only when it is shown, so
 * no value in the application carries an ambiguous unit.
 */
@JvmInline
value class Temperature private constructor(val celsius: Double) {

    val fahrenheit: Double
        get() = celsius * 9.0 / 5.0 + 32.0

    fun inUnit(unit: TemperatureUnit): Double = when (unit) {
        TemperatureUnit.CELSIUS -> celsius
        TemperatureUnit.FAHRENHEIT -> fahrenheit
    }

    fun roundedTo(unit: TemperatureUnit): Int = inUnit(unit).roundToInt()

    operator fun compareTo(other: Temperature): Int = celsius.compareTo(other.celsius)

    companion object {
        fun ofCelsius(value: Double): Temperature = Temperature(value)
    }
}
