package com.ozcanorhandemirci.hava.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.ozcanorhandemirci.hava.core.sky.SkyConditions

/**
 * The sky the whole application is currently standing under.
 *
 * There is one backdrop, held above navigation, rather than one per screen.
 * Three reasons, in order of how much they matter.
 *
 * It is what the interface is about. Moving from the list to a city, or from a
 * city to the search field, should not restart the weather; the sky is the same
 * sky, and the reader moved inside it.
 *
 * It is what makes every surface agree. The palette comes from this one place,
 * so a card, a chart and the navigation bar cannot end up lit by different
 * weather.
 *
 * And it is one canvas rather than four, animating once.
 */
@Stable
class AmbientSkyState {

    var conditions: SkyConditions? by mutableStateOf(null)
        private set

    fun show(conditions: SkyConditions) {
        this.conditions = conditions
    }
}

val LocalAmbientSky = staticCompositionLocalOf { AmbientSkyState() }

/**
 * Declares the sky this screen should be seen under.
 *
 * A screen that has an opinion states it; one that does not keeps whatever the
 * reader arrived with, which is why moving to the search field does not empty
 * the sky.
 */
@Composable
fun SkyOf(conditions: SkyConditions?) {
    val ambient = LocalAmbientSky.current
    SideEffect { if (conditions != null) ambient.show(conditions) }
}
