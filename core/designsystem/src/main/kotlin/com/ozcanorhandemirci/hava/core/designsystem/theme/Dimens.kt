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

/**
 * The measurements that decide how a window is filled.
 *
 * A window wider than a phone held upright is a different shape rather than a
 * larger one, and stretching a layout to fit it produces a card with its name
 * at one edge and its temperature at the other. These are the widths at which
 * the interface changes its mind, kept together because a screen that answers
 * one of them usually has to answer the rest.
 */
object HavaLayout {
    /**
     * The narrowest a weather card reads well at.
     *
     * Columns are counted from this rather than from a device category: a list
     * asks how many cards fit, and the answer is the same whether the width
     * came from a tablet, a landscape phone or a resized window.
     */
    val cardMinimumWidth = 340.dp

    /**
     * The width past which one city is shown as two panes.
     *
     * Below it the hero and the forecast share a single scrolling column. Above
     * it the reading stays in place on one side while the forecast moves on the
     * other, because scrolling a temperature off a screen that has room for it
     * is a waste of both.
     */
    val twoPaneWidth = 720.dp

    /** How wide a single column of prose or fields is allowed to grow. */
    val readableWidth = 560.dp

    /** The floating navigation bar, and the gap under the last card beneath it. */
    val navigationInset = 116.dp

    /** The bar floats rather than spans, so it stops growing before the window does. */
    val navigationBarMaxWidth = 420.dp
}
