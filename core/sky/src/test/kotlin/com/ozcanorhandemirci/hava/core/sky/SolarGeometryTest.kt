package com.ozcanorhandemirci.hava.core.sky

import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.shouldBe
import java.time.Instant
import kotlin.math.abs
import org.junit.Test

/**
 * The sun is checked against astronomy rather than against a previous run.
 *
 * At the moment it is highest, its height above the horizon is 90 degrees minus
 * the difference between the latitude and the declination of that day, and it
 * stands on the meridian: due south from the northern hemisphere, due north
 * from the southern one. Both facts are independent of how the position is
 * computed, which is what makes them worth asserting.
 */
class SolarGeometryTest {

    private val izmir = Place(latitude = 38.4237, longitude = 27.1428)
    private val tromso = Place(latitude = 69.6492, longitude = 18.9553)
    private val sydney = Place(latitude = -33.8688, longitude = 151.2093)

    /** Tilt of the earth, and therefore the declination at either solstice. */
    private val axialTilt = 23.44

    @Test
    fun `at the summer solstice the midday sun reaches its expected height`() {
        val noon = izmir.highestSunOn("2026-06-21")

        val expected = 90.0 - (izmir.latitude - axialTilt)
        abs(noon.altitudeDegrees - expected) shouldBeLessThan 0.5
    }

    @Test
    fun `at the winter solstice the same midday sun is far lower`() {
        val noon = izmir.highestSunOn("2026-12-21")

        val expected = 90.0 - (izmir.latitude + axialTilt)
        abs(noon.altitudeDegrees - expected) shouldBeLessThan 0.5
    }

    @Test
    fun `the northern midday sun stands due south`() {
        val noon = izmir.highestSunOn("2026-06-21")

        abs(noon.azimuthDegrees - 180.0) shouldBeLessThan 0.5
    }

    @Test
    fun `the southern midday sun stands due north`() {
        val noon = sydney.highestSunOn("2026-12-21")

        noon.altitudeDegrees shouldBeGreaterThan 60.0
        minOf(noon.azimuthDegrees, 360.0 - noon.azimuthDegrees) shouldBeLessThan 0.5
    }

    @Test
    fun `the sun is below the horizon at local midnight`() {
        val midnight = izmir.sunAt("2026-06-21T22:10:00Z")

        midnight.altitudeDegrees shouldBeLessThan 0.0
        midnight.isAboveHorizon shouldBe false
    }

    @Test
    fun `inside the arctic circle the midsummer sun never sets`() {
        val lowest = (0 until HOURS_IN_DAY).minOf { hour ->
            tromso.sunAt("2026-06-21T%02d:00:00Z".format(hour)).altitudeDegrees
        }

        lowest shouldBeGreaterThan 0.0
    }

    @Test
    fun `at the equinox day and night are close to equal at the equator`() {
        val quito = Place(latitude = -0.1807, longitude = -78.4678)

        val hoursOfDaylight = (0 until MINUTES_IN_DAY)
            .count { minute -> quito.sunAt("2026-03-20T00:00:00Z", minute).isAboveHorizon }
            .toDouble() / MINUTES_IN_HOUR

        abs(hoursOfDaylight - 12.0) shouldBeLessThan 0.3
    }

    private data class Place(val latitude: Double, val longitude: Double)

    private fun Place.sunAt(instant: String, plusMinutes: Int = 0): SolarPosition =
        SolarGeometry.positionAt(
            instant = Instant.parse(instant).plusSeconds(plusMinutes * SECONDS_IN_MINUTE),
            latitude = latitude,
            longitude = longitude,
        )

    /** Searches the day a minute at a time rather than assuming when noon falls. */
    private fun Place.highestSunOn(date: String): SolarPosition =
        (0 until MINUTES_IN_DAY)
            .map { minute -> sunAt("${date}T00:00:00Z", minute) }
            .maxBy { it.altitudeDegrees }

    private companion object {
        const val SECONDS_IN_MINUTE = 60L
        const val MINUTES_IN_HOUR = 60.0
        const val MINUTES_IN_DAY = 1440
        const val HOURS_IN_DAY = 24
    }
}
