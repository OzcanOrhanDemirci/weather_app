package com.ozcanorhandemirci.hava

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ozcanorhandemirci.hava.feature.cities.CitiesDestination
import com.ozcanorhandemirci.hava.feature.cities.CitiesRoute
import com.ozcanorhandemirci.hava.feature.detail.CityDetailDestination
import com.ozcanorhandemirci.hava.feature.detail.CityDetailRoute

/**
 * Root of the interface.
 *
 * Opening a city grows into it rather than sliding a new page across. Both
 * screens are drawn over the sky of the same place, so a horizontal slide would
 * move a backdrop that has not changed, and the eye would read it as two
 * separate skies passing one another. Scaling up from the card keeps it as one.
 */
@Composable
fun HavaApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = CitiesDestination,
        modifier = modifier,
        enterTransition = {
            scaleIn(initialScale = OPENING_SCALE, animationSpec = tween(TRANSITION_MILLIS)) +
                fadeIn(animationSpec = tween(TRANSITION_MILLIS))
        },
        exitTransition = {
            scaleOut(targetScale = RECEDING_SCALE, animationSpec = tween(TRANSITION_MILLIS)) +
                fadeOut(animationSpec = tween(TRANSITION_MILLIS))
        },
        popEnterTransition = {
            scaleIn(initialScale = RECEDING_SCALE, animationSpec = tween(TRANSITION_MILLIS)) +
                fadeIn(animationSpec = tween(TRANSITION_MILLIS))
        },
        popExitTransition = {
            scaleOut(targetScale = OPENING_SCALE, animationSpec = tween(TRANSITION_MILLIS)) +
                fadeOut(animationSpec = tween(TRANSITION_MILLIS))
        },
    ) {
        composable<CitiesDestination> {
            CitiesRoute(
                onCitySelected = { cityId -> navController.navigate(CityDetailDestination(cityId)) },
            )
        }

        composable<CityDetailDestination> {
            CityDetailRoute(onBack = navController::popBackStack)
        }
    }
}

/** The screen being opened starts slightly small, as though it came from the card. */
private const val OPENING_SCALE = 0.90f

/** The screen being left settles back rather than standing still behind. */
private const val RECEDING_SCALE = 1.06f

private const val TRANSITION_MILLIS = 320
