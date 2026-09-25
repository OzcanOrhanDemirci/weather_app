package com.ozcanorhandemirci.hava.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaLayout
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaSpacing

/**
 * A list of cities, in as many columns as the window has room for.
 *
 * The count is not chosen per device. It follows from the narrowest width a
 * card still reads at, so an upright phone shows one column, the same phone
 * turned on its side shows two, and a card is never stretched until its name
 * and its temperature sit at opposite edges with nothing between them.
 *
 * The heading and the banner span the full width whatever that count is. They
 * belong to the list rather than to a position in it, and a title occupying
 * the first cell of a grid would read as its first entry.
 *
 * Both screens that show cities share this. They differ in what they say above
 * the cards, not in how the cards are arranged, and a second copy of the
 * arrangement would be a second place for the column count to be wrong.
 */
@Composable
fun CityGrid(
    cities: List<CitySummary>,
    onCitySelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    banner: (@Composable () -> Unit)? = null,
    header: @Composable () -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(HavaLayout.cardMinimumWidth),
        state = state,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = HavaSpacing.gutter,
            end = HavaSpacing.gutter,
            // Room for the floating navigation bar, which stands over the list
            // rather than beside it.
            bottom = HavaLayout.navigationInset,
        ),
        verticalArrangement = Arrangement.spacedBy(HavaSpacing.compact),
        horizontalArrangement = Arrangement.spacedBy(HavaSpacing.compact),
    ) {
        item(key = "header", span = { GridItemSpan(maxLineSpan) }) { header() }

        if (banner != null) {
            item(key = "banner", span = { GridItemSpan(maxLineSpan) }) { banner() }
        }

        items(items = cities, key = { it.city.id }) { summary ->
            CityWeatherCard(summary = summary, onClick = { onCitySelected(summary.city.id) })
        }
    }
}
