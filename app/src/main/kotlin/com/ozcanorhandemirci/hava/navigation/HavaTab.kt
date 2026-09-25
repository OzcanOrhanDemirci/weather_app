package com.ozcanorhandemirci.hava.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.ozcanorhandemirci.hava.R
import com.ozcanorhandemirci.hava.feature.cities.CitiesDestination
import com.ozcanorhandemirci.hava.feature.favorites.FavoritesDestination
import com.ozcanorhandemirci.hava.feature.search.SearchDestination

/**
 * The three places a reader can be.
 *
 * Declared as a list rather than written into the bar by hand, so the bar and
 * the graph cannot disagree about what exists.
 */
enum class HavaTab(
    val destination: Any,
    @param:StringRes val label: Int,
    val icon: ImageVector,
) {
    Cities(CitiesDestination, R.string.tab_cities, Icons.Filled.List),
    Favorites(FavoritesDestination, R.string.tab_favorites, Icons.Filled.Favorite),
    Search(SearchDestination, R.string.tab_search, Icons.Filled.Search),
}
