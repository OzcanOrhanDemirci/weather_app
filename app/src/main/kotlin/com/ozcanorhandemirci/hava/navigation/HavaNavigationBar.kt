package com.ozcanorhandemirci.hava.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaLayout
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaMotion
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaSpacing
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaTheme
import com.ozcanorhandemirci.hava.core.designsystem.theme.respectingReducedMotion

/**
 * The bar that moves between the three tabs.
 *
 * A floating pane rather than a bar across the bottom of the window, because
 * the sky runs to the edge of the screen and a solid bar would cut it off.
 * Like every other surface here it takes its fill from the weather, so it
 * darkens at noon and lightens at night without being told.
 *
 * It stops growing well before a wide window does. Three icons spread over the
 * width of a landscape screen read as three separate targets rather than one
 * control, and every gap between them is distance a thumb has to travel.
 */
@Composable
fun HavaNavigationBar(
    selected: HavaTab,
    onSelect: (HavaTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sky = HavaTheme.sky

    Row(
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = HavaSpacing.section, vertical = HavaSpacing.compact)
            .widthIn(max = HavaLayout.navigationBarMaxWidth)
            .fillMaxWidth()
            .clip(RoundedCornerShape(BAR_CORNER))
            .background(sky.glass)
            .border(1.dp, sky.glassEdge, RoundedCornerShape(BAR_CORNER))
            .padding(vertical = HavaSpacing.small),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HavaTab.entries.forEach { tab ->
            val isSelected = tab == selected
            val tint by animateColorAsState(
                targetValue = if (isSelected) sky.accent else sky.contentMuted,
                animationSpec = respectingReducedMotion(HavaMotion.standard()),
                label = "tab",
            )

            Icon(
                imageVector = tab.icon,
                contentDescription = stringResource(tab.label),
                tint = tint,
                modifier = Modifier
                    .clip(RoundedCornerShape(ITEM_CORNER))
                    .selectable(
                        selected = isSelected,
                        role = Role.Tab,
                        onClick = { onSelect(tab) },
                    )
                    .padding(horizontal = HavaSpacing.large, vertical = HavaSpacing.small),
            )
        }
    }
}

private val BAR_CORNER = 28.dp
private val ITEM_CORNER = 20.dp
