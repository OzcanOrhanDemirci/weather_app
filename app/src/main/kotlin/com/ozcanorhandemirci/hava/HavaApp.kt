package com.ozcanorhandemirci.hava

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaTheme
import com.ozcanorhandemirci.hava.core.designsystem.theme.SkyPalette
import com.ozcanorhandemirci.hava.core.sky.Sky
import com.ozcanorhandemirci.hava.core.sky.SkyDetail
import com.ozcanorhandemirci.hava.core.sky.animateSkyPalette
import com.ozcanorhandemirci.hava.core.sky.rememberSkyState
import com.ozcanorhandemirci.hava.core.ui.AmbientSkyState
import com.ozcanorhandemirci.hava.core.ui.LocalAmbientSky
import com.ozcanorhandemirci.hava.feature.cities.CitiesDestination
import com.ozcanorhandemirci.hava.feature.cities.CitiesRoute
import com.ozcanorhandemirci.hava.feature.detail.CityDetailDestination
import com.ozcanorhandemirci.hava.feature.detail.CityDetailRoute
import com.ozcanorhandemirci.hava.feature.favorites.FavoritesDestination
import com.ozcanorhandemirci.hava.feature.favorites.FavoritesRoute
import com.ozcanorhandemirci.hava.feature.search.SearchDestination
import com.ozcanorhandemirci.hava.feature.search.SearchRoute
import com.ozcanorhandemirci.hava.navigation.HavaNavigationBar
import com.ozcanorhandemirci.hava.navigation.HavaTab

/**
 * Root of the interface.
 *
 * One sky is drawn here, above navigation, and every screen is placed on it.
 * Screens state which weather they want to be seen under and the backdrop
 * travels to it, so moving from the list to a city, or from a city to the
 * search field, does not restart the sky: the reader moved inside it.
 *
 * Opening a city therefore grows into it rather than sliding a new page across.
 * A horizontal slide would move a backdrop that has not changed, and the eye
 * would read it as two skies passing one another.
 *
 * The navigation bar leaves with the tabs. A city is a place a reader went into
 * rather than one of three places they can be, and offering to switch tabs from
 * inside it would say otherwise.
 */
@Composable
fun HavaApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination

    val selectedTab = HavaTab.entries.firstOrNull { tab ->
        destination?.hierarchy?.any { it.hasRoute(tab.destination::class) } == true
    }

    val ambientSky = remember { AmbientSkyState() }
    val skyState = ambientSky.conditions?.let { rememberSkyState(it) }
    val palette = animateSkyPalette(skyState?.palette ?: SkyPalette.Placeholder)

    val openCity: (Long) -> Unit = { cityId ->
        navController.navigate(CityDetailDestination(cityId))
    }

    CompositionLocalProvider(LocalAmbientSky provides ambientSky) {
        HavaTheme(palette = palette) {
            Box(modifier = modifier.fillMaxSize()) {
                if (skyState != null) {
                    Sky(
                        state = skyState,
                        detail = SkyDetail.Backdrop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                NavHost(
                    navController = navController,
                    startDestination = CitiesDestination,
                    modifier = Modifier.fillMaxSize(),
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
                        CitiesRoute(onCitySelected = openCity)
                    }

                    composable<FavoritesDestination> {
                        FavoritesRoute(onCitySelected = openCity)
                    }

                    composable<SearchDestination> {
                        SearchRoute(onCityOpened = openCity)
                    }

                    composable<CityDetailDestination> {
                        CityDetailRoute(onBack = navController::popBackStack)
                    }
                }

                AnimatedVisibility(
                    visible = selectedTab != null,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut(),
                    modifier = Modifier.align(Alignment.BottomCenter),
                ) {
                    HavaNavigationBar(
                        selected = selectedTab ?: HavaTab.Cities,
                        onSelect = { tab ->
                            navController.navigate(tab.destination) {
                                // One entry per tab, and returning to a tab
                                // returns to where it was left rather than to
                                // the top of it.
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            }
        }
    }
}

/** The screen being opened starts slightly small, as though it came from the card. */
private const val OPENING_SCALE = 0.90f

/** The screen being left settles back rather than standing still behind. */
private const val RECEDING_SCALE = 1.06f

private const val TRANSITION_MILLIS = 320
