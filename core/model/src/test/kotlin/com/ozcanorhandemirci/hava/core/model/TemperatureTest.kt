package com.ozcanorhandemirci.hava.core.model

import io.kotest.matchers.shouldBe
import org.junit.Test

class TemperatureTest {

    @Test
    fun `freezing point converts to fahrenheit`() {
        Temperature.ofCelsius(0.0).fahrenheit shouldBe 32.0
    }

    @Test
    fun `rounding follows the requested unit`() {
        val temperature = Temperature.ofCelsius(21.6)

        temperature.roundedTo(TemperatureUnit.CELSIUS) shouldBe 22
        temperature.roundedTo(TemperatureUnit.FAHRENHEIT) shouldBe 71
    }

    @Test
    fun `temperatures order by their canonical value`() {
        (Temperature.ofCelsius(10.0) < Temperature.ofCelsius(20.0)) shouldBe true
    }
}
