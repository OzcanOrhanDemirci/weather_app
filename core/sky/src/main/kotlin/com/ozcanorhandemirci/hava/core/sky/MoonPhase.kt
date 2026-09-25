package com.ozcanorhandemirci.hava.core.sky

import java.time.Instant
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * How much of the moon is lit, and on which side.
 *
 * [fraction] runs from 0 at new moon through 0.5 at full moon and back to 1,
 * which is new again. [illumination] is the lit share of the disc, from 0 to 1.
 * [isWaxing] says whether the lit edge is on the right, as seen from the
 * northern hemisphere.
 */
data class MoonPhase(
    val fraction: Double,
    val illumination: Double,
    val isWaxing: Boolean,
) {
    val isNew: Boolean get() = illumination < 0.02
    val isFull: Boolean get() = illumination > 0.98
}

/**
 * The phase of the moon.
 *
 * A mean synodic month is used rather than a full lunar theory. The real month
 * varies by a few hours around the mean, so the phase can be off by roughly
 * half a day at worst. Drawn as a disc a few millimetres across, that error is
 * invisible, while the difference between drawing the true crescent and drawing
 * the same circle every night is not.
 */
object LunarGeometry {

    /** Mean length of one lunation, in days. */
    private const val SYNODIC_MONTH_DAYS = 29.530588853

    /** A new moon that actually happened: 6 January 2000, 18:14 UTC. */
    private const val REFERENCE_NEW_MOON_EPOCH_SECOND = 947_182_440L

    private const val SECONDS_PER_DAY = 86_400.0

    fun phaseAt(instant: Instant): MoonPhase {
        val daysSinceReference =
            (instant.epochSecond - REFERENCE_NEW_MOON_EPOCH_SECOND) / SECONDS_PER_DAY
        val fraction = (daysSinceReference / SYNODIC_MONTH_DAYS).mod(1.0)

        // The terminator follows a cosine, so illumination is not linear in the
        // phase: the moon spends longer looking nearly full than nearly half.
        val illumination = (1.0 - cos(2.0 * Math.PI * fraction)) / 2.0

        return MoonPhase(
            fraction = fraction,
            illumination = illumination.coerceIn(0.0, 1.0),
            isWaxing = fraction < 0.5,
        )
    }

    /**
     * Signed width of the terminator as a share of the radius.
     *
     * Zero at the quarters, where the edge is a straight line. Positive when the
     * lit part bulges outward, which is what turns a crescent into a gibbous.
     */
    fun terminatorCurvature(phase: MoonPhase): Double =
        abs(cos(2.0 * Math.PI * phase.fraction)) * if (phase.illumination > 0.5) 1.0 else -1.0

    /**
     * Where the moon is, seen from one place at one moment.
     *
     * The moon is placed by its phase: a new moon sits with the sun, a full moon
     * sits opposite it, and the quarters sit a right angle away. Its orbit is
     * treated as lying in the plane of the ecliptic, which it misses by about
     * five degrees, and the position is therefore good to a few degrees rather
     * than to a minute of arc.
     *
     * That is enough for the thing this needs to be right about: the moon rises
     * late in its first quarter and early in its last, so a crescent hangs near
     * the horizon after sunset while a full moon climbs as the night goes on.
     */
    fun positionAt(instant: Instant, latitude: Double, longitude: Double): SolarPosition {
        val century = SolarGeometry.julianCentury(instant)
        val obliquity = Math.toRadians(SolarGeometry.obliquityOfEcliptic(century))

        val eclipticLongitude = Math.toRadians(
            (SolarGeometry.apparentSolarLongitude(century) + FULL_TURN * phaseAt(instant).fraction)
                .mod(FULL_TURN),
        )

        val rightAscension = Math.toDegrees(
            atan2(cos(obliquity) * sin(eclipticLongitude), cos(eclipticLongitude)),
        ).mod(FULL_TURN)
        val declination = Math.toDegrees(asin(sin(obliquity) * sin(eclipticLongitude)))

        return SolarGeometry.toHorizon(
            rightAscensionDegrees = rightAscension,
            declinationDegrees = declination,
            instant = instant,
            latitude = latitude,
            longitude = longitude,
        )
    }

    private const val FULL_TURN = 360.0
}
