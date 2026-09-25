package com.ozcanorhandemirci.hava.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * The corner scale.
 *
 * Radii grow with the size of the surface so that a small chip and a full
 * width card read as members of the same family rather than two unrelated
 * rectangles.
 */
internal val HavaShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

/** The spacing scale. Every gap in the application is one of these. */
object HavaSpacing {
    val hairline = 1.dp
    val tiny = 4.dp
    val small = 8.dp
    val compact = 12.dp
    val medium = 16.dp
    val large = 24.dp
    val huge = 32.dp
    val section = 40.dp

    /** Distance from content to the side of the screen. */
    val gutter = 20.dp
}
