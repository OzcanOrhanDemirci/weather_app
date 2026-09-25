package com.ozcanorhandemirci.hava

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ozcanorhandemirci.hava.feature.cities.CitiesRoute

/**
 * Root of the interface.
 *
 * The list of cities is the entry point. Opening one arrives with the detail
 * screen, which is the next thing to be built.
 */
@Composable
fun HavaApp(modifier: Modifier = Modifier) {
    CitiesRoute(
        onCitySelected = { },
        modifier = modifier,
    )
}
