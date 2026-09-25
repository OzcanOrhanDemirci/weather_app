package com.ozcanorhandemirci.hava

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.ozcanorhandemirci.hava.core.designsystem.modifier.skyGradient
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaSpacing
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaTheme

/**
 * Root of the interface.
 *
 * The backdrop is the first thing the application commits to, so it is drawn
 * here and the screens are placed over it. Navigation and the city list arrive
 * in this position.
 */
@Composable
fun HavaApp(modifier: Modifier = Modifier) {
    val sky = HavaTheme.sky

    Box(
        modifier = modifier
            .fillMaxSize()
            .skyGradient(sky),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(HavaSpacing.small),
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayLarge,
                color = sky.content,
            )
            Text(
                text = stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = sky.contentMuted,
                textAlign = TextAlign.Center,
            )
        }
    }
}
