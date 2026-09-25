package com.ozcanorhandemirci.hava.core.model

import io.kotest.matchers.shouldBe
import org.junit.Test

class WeatherKindTest {

    @Test
    fun `clear sky reports no precipitation and no cloud`() {
        val kind = WeatherKind.fromWmoCode(0)

        kind.condition shouldBe WeatherCondition.CLEAR
        kind.precipitation shouldBe Precipitation.NONE
        kind.cloudCover shouldBe CloudCover.CLEAR
        kind.hasLightning shouldBe false
    }

    @Test
    fun `heavy rain keeps its intensity`() {
        val kind = WeatherKind.fromWmoCode(65)

        kind.condition shouldBe WeatherCondition.RAIN
        kind.intensity shouldBe Intensity.HEAVY
        kind.precipitation shouldBe Precipitation.RAIN
    }

    @Test
    fun `snow showers fall as snow`() {
        WeatherKind.fromWmoCode(86).precipitation shouldBe Precipitation.SNOW
    }

    @Test
    fun `freezing rain is treated as sleet`() {
        WeatherKind.fromWmoCode(66).precipitation shouldBe Precipitation.SLEET
    }

    @Test
    fun `thunderstorms carry lightning`() {
        WeatherKind.fromWmoCode(95).hasLightning shouldBe true
        WeatherKind.fromWmoCode(99).hasLightning shouldBe true
    }

    @Test
    fun `fog reduces visibility`() {
        WeatherKind.fromWmoCode(45).reducesVisibility shouldBe true
    }

    @Test
    fun `an unpublished code degrades instead of failing`() {
        val kind = WeatherKind.fromWmoCode(Int.MAX_VALUE)

        kind.condition shouldBe WeatherCondition.UNKNOWN
        kind.precipitation shouldBe Precipitation.NONE
    }
}
