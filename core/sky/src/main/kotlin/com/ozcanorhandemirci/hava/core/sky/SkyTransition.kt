package com.ozcanorhandemirci.hava.core.sky

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaMotion
import com.ozcanorhandemirci.hava.core.designsystem.theme.LocalReducedMotion

/**
 * How fast the sky should move to where it is going.
 *
 * Two different things change the conditions, and they want opposite speeds.
 *
 * Arriving at a different place is a journey. The whole backdrop changes at
 * once, and it should take its time: a cut between two skies reads as a glitch,
 * while the same change spread over nearly two seconds reads as weather.
 *
 * Changing the hour within one place is a scrub. The reader is holding the hour
 * under their finger, and a backdrop that arrives two seconds later has stopped
 * answering them.
 *
 * The choice is carried rather than a single specification, because the same
 * decision has to produce one for colors and another for the numbers that drive
 * the layers, and an animation specification is bound to the type it animates.
 */
@Immutable
class SkyTransition internal constructor(
    private val isScrub: Boolean,
    private val isReduced: Boolean,
) {

    fun <T> spec(): FiniteAnimationSpec<T> = when {
        isReduced -> snap()
        isScrub -> HavaMotion.responsive()
        else -> HavaMotion.atmospheric()
    }
}

/**
 * Chooses the speed from what actually changed.
 *
 * An earlier version started a timer whenever the place changed and treated
 * everything within it as part of the journey. That was wrong in a way worth
 * recording: opening a city and immediately dragging the hourly curve meant the
 * drag inherited the slow speed and lagged a second behind the finger.
 *
 * The hour the place was arrived at is remembered instead. While the hour still
 * matches it, nothing has been scrubbed and the sky is travelling; the moment it
 * differs, the reader is moving time and the sky follows them directly.
 */
@Composable
fun rememberSkyTransition(conditions: SkyConditions?): SkyTransition {
    val place = conditions?.coordinates
    val instant = conditions?.instant

    val hourOfArrival = remember(place) { instant }
    val isScrub = instant != hourOfArrival

    val reduced = LocalReducedMotion.current
    return remember(isScrub, reduced) { SkyTransition(isScrub, reduced) }
}
