package com.ozcanorhandemirci.hava.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaSpacing
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaTheme

/**
 * The surface that content sits on.
 *
 * Nothing in the application is placed directly on the sky. The sky changes
 * from a white noon to a black midnight, and text that is legible on one is
 * invisible on the other. A translucent pane solves that once: it carries its
 * own contrast while still letting the weather through.
 *
 * The pane is lit from the top, as glass is, so it reads as a physical layer
 * rather than a rectangle of transparency.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    contentPadding: PaddingValues = PaddingValues(HavaSpacing.medium),
    content: @Composable BoxScope.() -> Unit,
) {
    val sky = HavaTheme.sky

    val fill = Brush.verticalGradient(
        colors = listOf(
            sky.glass.lightenBy(GLASS_TOP_LIFT),
            sky.glass,
        ),
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(fill)
            .border(width = 1.dp, color = sky.glassEdge, shape = shape)
            .padding(contentPadding),
        content = content,
    )
}

/** Alpha added to the top of a pane so the edge catches the light. */
private const val GLASS_TOP_LIFT = 0.06f

private fun Color.lightenBy(amount: Float): Color =
    copy(alpha = (alpha + amount).coerceAtMost(1f))
