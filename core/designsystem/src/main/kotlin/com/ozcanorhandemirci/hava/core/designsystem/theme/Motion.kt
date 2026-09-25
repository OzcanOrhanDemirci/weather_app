package com.ozcanorhandemirci.hava.core.designsystem.theme

import android.provider.Settings
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

/**
 * Motion is described by springs rather than durations.
 *
 * A duration says how long a movement takes regardless of how far it travels
 * and regardless of what it was doing a moment ago. A spring carries velocity,
 * so a gesture that is reversed halfway does not restart: it turns around. Every
 * transition in the application is interruptible for that reason.
 */
object HavaMotion {

    /** Settling with no overshoot. For color, opacity and anything read as text. */
    fun <T> gentle(): SpringSpec<T> = spring(dampingRatio = 0.92f, stiffness = 220f)

    /** The default for position and size changes. */
    fun <T> standard(): SpringSpec<T> = spring(dampingRatio = 0.85f, stiffness = 420f)

    /** Visible overshoot. Reserved for a change the user caused directly. */
    fun <T> expressive(): SpringSpec<T> = spring(dampingRatio = 0.62f, stiffness = 300f)

    /** Short and firm, for controls that must feel immediate under a finger. */
    fun <T> snappy(): SpringSpec<T> = spring(dampingRatio = 0.80f, stiffness = 900f)

    /**
     * The sky travelling from one place to another.
     *
     * Much slower than anything else here, and with no overshoot at all. A
     * change of weather is not a control responding to a tap; it is the light
     * changing, and light does not snap.
     *
     * Critically damped, so the settling time follows from the stiffness alone:
     * a spring at this stiffness has a natural frequency near 3.7 radians per
     * second and is within one per cent of its target after roughly 1.8
     * seconds. That is long enough to read as movement rather than as a cut,
     * and short enough that a reader scrolling deliberately is not waiting for
     * the sky to catch up.
     */
    fun <T> atmospheric(): SpringSpec<T> = spring(dampingRatio = 1f, stiffness = 14f)

    /**
     * The sky following a finger within one place.
     *
     * Dragging the hourly curve is direct manipulation: the reader is holding
     * the hour, and a backdrop that arrives two seconds after the finger reads
     * as broken rather than as calm. Still critically damped, so it settles
     * without a bounce, and near a third of a second rather than instant, which
     * keeps a dragged finger from making the light flicker.
     */
    fun <T> responsive(): SpringSpec<T> = spring(dampingRatio = 1f, stiffness = 400f)
}

/**
 * True when the device is set to remove animation.
 *
 * Provided through the theme rather than read at each call site so a component
 * cannot forget to honour it.
 */
val LocalReducedMotion = staticCompositionLocalOf { false }

@Composable
internal fun rememberSystemReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        val scale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        )
        scale == 0f
    }
}

/**
 * Returns [spec], or an immediate transition when the device asks for reduced
 * motion. The animated value still reaches the same place, so state stays
 * correct while the movement disappears.
 */
@Composable
fun <T> respectingReducedMotion(spec: FiniteAnimationSpec<T>): FiniteAnimationSpec<T> =
    if (LocalReducedMotion.current) snap() else spec
