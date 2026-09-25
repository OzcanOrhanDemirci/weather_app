package com.ozcanorhandemirci.hava.core.sky

import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/**
 * Where the sun is, seen from one place at one moment.
 *
 * [altitudeDegrees] is measured from the horizon: positive above it, negative
 * below. [azimuthDegrees] is measured clockwise from true north.
 */
data class SolarPosition(
    val altitudeDegrees: Double,
    val azimuthDegrees: Double,
) {
    val isAboveHorizon: Boolean get() = altitudeDegrees > 0.0
}

/**
 * The position of the sun, from the algorithm published by the NOAA Global
 * Monitoring Laboratory.
 *
 * The backdrop could have faked this by sliding a disc between the sunrise and
 * sunset the forecast reports, but that arc would be wrong in every way that is
 * visible: too high in winter, too low in summer, and symmetric about noon when
 * the real one is not. Computing the true position costs a handful of
 * trigonometric calls per frame and makes the sky belong to its place.
 *
 * Accuracy is within roughly a tenth of a degree for the years this application
 * will be used in, which is far below what a screen can show.
 */
object SolarGeometry {

    /** The earth turns one degree of longitude every four minutes. */
    private const val MINUTES_PER_DEGREE_OF_LONGITUDE = 4.0
    private const val MINUTES_PER_DAY = 1440.0
    private const val JULIAN_DAY_AT_EPOCH = 2440587.5
    private const val SECONDS_PER_DAY = 86_400.0
    private const val JULIAN_DAY_J2000 = 2451545.0
    private const val DAYS_PER_JULIAN_CENTURY = 36525.0

    fun positionAt(instant: Instant, latitude: Double, longitude: Double): SolarPosition {
        val century = julianCentury(instant)

        val meanLongitude = geometricMeanLongitude(century)
        val meanAnomaly = geometricMeanAnomaly(century)
        val eccentricity = orbitEccentricity(century)
        val apparentLongitude = apparentLongitude(meanLongitude, meanAnomaly, century)
        val obliquity = correctedObliquity(century)

        val declination = Math.toDegrees(
            asin(sin(Math.toRadians(obliquity)) * sin(Math.toRadians(apparentLongitude))),
        )

        val minutesUtc = minutesIntoUtcDay(instant)
        val equationOfTime = equationOfTime(meanLongitude, meanAnomaly, eccentricity, obliquity)
        val trueSolarTime = (minutesUtc + equationOfTime + MINUTES_PER_DEGREE_OF_LONGITUDE * longitude)
            .mod(MINUTES_PER_DAY)

        val hourAngle = (trueSolarTime / 4.0).let { if (it < 0.0) it + 180.0 else it - 180.0 }

        val latitudeRadians = Math.toRadians(latitude)
        val declinationRadians = Math.toRadians(declination)
        val hourAngleRadians = Math.toRadians(hourAngle)

        val cosZenith = (
            sin(latitudeRadians) * sin(declinationRadians) +
                cos(latitudeRadians) * cos(declinationRadians) * cos(hourAngleRadians)
            ).coerceIn(-1.0, 1.0)
        val zenith = Math.toDegrees(acos(cosZenith))

        return SolarPosition(
            altitudeDegrees = 90.0 - zenith,
            azimuthDegrees = azimuth(latitudeRadians, zenith, declinationRadians, hourAngle),
        )
    }

    private fun azimuth(
        latitudeRadians: Double,
        zenithDegrees: Double,
        declinationRadians: Double,
        hourAngleDegrees: Double,
    ): Double {
        val zenithRadians = Math.toRadians(zenithDegrees)
        val denominator = cos(latitudeRadians) * sin(zenithRadians)

        // Directly overhead or at the pole, where azimuth is undefined.
        if (denominator == 0.0) return 0.0

        val ratio = (
            (sin(latitudeRadians) * cos(zenithRadians) - sin(declinationRadians)) / denominator
            ).coerceIn(-1.0, 1.0)
        val angle = Math.toDegrees(acos(ratio))

        return if (hourAngleDegrees > 0.0) (angle + 180.0).mod(360.0) else (540.0 - angle).mod(360.0)
    }

    internal fun julianDay(instant: Instant): Double =
        instant.epochSecond / SECONDS_PER_DAY + JULIAN_DAY_AT_EPOCH

    internal fun julianCentury(instant: Instant): Double =
        (julianDay(instant) - JULIAN_DAY_J2000) / DAYS_PER_JULIAN_CENTURY

    /** Ecliptic longitude of the sun, which the moon is positioned relative to. */
    internal fun apparentSolarLongitude(century: Double): Double =
        apparentLongitude(geometricMeanLongitude(century), geometricMeanAnomaly(century), century)

    internal fun obliquityOfEcliptic(century: Double): Double = correctedObliquity(century)

    /**
     * Greenwich mean sidereal time: the right ascension currently on the
     * Greenwich meridian. Turning a position on the sky into a position over a
     * particular place needs it.
     */
    internal fun greenwichMeanSiderealTime(instant: Instant): Double =
        (280.46061837 + 360.98564736629 * (julianDay(instant) - JULIAN_DAY_J2000)).mod(360.0)

    /**
     * Converts a position on the celestial sphere into one above a horizon.
     *
     * Shared by the sun and the moon, because after each has been located among
     * the stars the remaining arithmetic is the same.
     */
    internal fun toHorizon(
        rightAscensionDegrees: Double,
        declinationDegrees: Double,
        instant: Instant,
        latitude: Double,
        longitude: Double,
    ): SolarPosition {
        val hourAngle = (greenwichMeanSiderealTime(instant) + longitude - rightAscensionDegrees)
            .mod(360.0)
            .let { if (it > 180.0) it - 360.0 else it }

        val latitudeRadians = Math.toRadians(latitude)
        val declinationRadians = Math.toRadians(declinationDegrees)
        val hourAngleRadians = Math.toRadians(hourAngle)

        val sinAltitude = (
            sin(latitudeRadians) * sin(declinationRadians) +
                cos(latitudeRadians) * cos(declinationRadians) * cos(hourAngleRadians)
            ).coerceIn(-1.0, 1.0)
        val altitude = Math.toDegrees(asin(sinAltitude))

        val zenith = 90.0 - altitude
        return SolarPosition(
            altitudeDegrees = altitude,
            azimuthDegrees = azimuth(latitudeRadians, zenith, declinationRadians, hourAngle),
        )
    }

    private fun minutesIntoUtcDay(instant: Instant): Double {
        val utc = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC)
        return utc.hour * 60.0 + utc.minute + utc.second / 60.0
    }

    private fun geometricMeanLongitude(century: Double): Double =
        (280.46646 + century * (36000.76983 + century * 0.0003032)).mod(360.0)

    private fun geometricMeanAnomaly(century: Double): Double =
        357.52911 + century * (35999.05029 - 0.0001537 * century)

    private fun orbitEccentricity(century: Double): Double =
        0.016708634 - century * (0.000042037 + 0.0000001267 * century)

    private fun equationOfCenter(meanAnomaly: Double, century: Double): Double {
        val anomaly = Math.toRadians(meanAnomaly)
        return sin(anomaly) * (1.914602 - century * (0.004817 + 0.000014 * century)) +
            sin(2.0 * anomaly) * (0.019993 - 0.000101 * century) +
            sin(3.0 * anomaly) * 0.000289
    }

    private fun apparentLongitude(meanLongitude: Double, meanAnomaly: Double, century: Double): Double {
        val trueLongitude = meanLongitude + equationOfCenter(meanAnomaly, century)
        return trueLongitude - 0.00569 - 0.00478 * sin(Math.toRadians(125.04 - 1934.136 * century))
    }

    private fun correctedObliquity(century: Double): Double {
        val mean = 23.0 +
            (26.0 + (21.448 - century * (46.815 + century * (0.00059 - century * 0.001813))) / 60.0) / 60.0
        return mean + 0.00256 * cos(Math.toRadians(125.04 - 1934.136 * century))
    }

    private fun equationOfTime(
        meanLongitude: Double,
        meanAnomaly: Double,
        eccentricity: Double,
        obliquity: Double,
    ): Double {
        val y = tan(Math.toRadians(obliquity / 2.0)).let { it * it }
        val longitude = Math.toRadians(meanLongitude)
        val anomaly = Math.toRadians(meanAnomaly)

        val minutes = y * sin(2.0 * longitude) -
            2.0 * eccentricity * sin(anomaly) +
            4.0 * eccentricity * y * sin(anomaly) * cos(2.0 * longitude) -
            0.5 * y * y * sin(4.0 * longitude) -
            1.25 * eccentricity * eccentricity * sin(2.0 * anomaly)

        return 4.0 * Math.toDegrees(minutes)
    }
}
