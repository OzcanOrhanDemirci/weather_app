package com.ozcanorhandemirci.hava.feature.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaSpacing
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaTheme
import com.ozcanorhandemirci.hava.core.ui.CityGrid

@Composable
fun FavoritesRoute(
    onCitySelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FavoritesScreen(state = state, onCitySelected = onCitySelected, modifier = modifier)
}

@Composable
internal fun FavoritesScreen(
    state: FavoritesUiState,
    onCitySelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (state) {
            FavoritesUiState.Loading -> CircularProgressIndicator(
                color = HavaTheme.sky.content,
                modifier = Modifier.align(Alignment.Center),
            )

            FavoritesUiState.Empty -> Column(
                modifier = Modifier.align(Alignment.Center).padding(HavaSpacing.section),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(HavaSpacing.small),
            ) {
                Text(
                    text = stringResource(R.string.favorites_empty_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = HavaTheme.sky.content,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.favorites_empty_body),
                    style = MaterialTheme.typography.bodyLarge,
                    color = HavaTheme.sky.contentMuted,
                    textAlign = TextAlign.Center,
                )
            }

            is FavoritesUiState.Content -> CityGrid(
                cities = state.cities,
                onCitySelected = onCitySelected,
            ) {
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(top = HavaSpacing.large, bottom = HavaSpacing.small),
                ) {
                    Text(
                        text = stringResource(R.string.favorites_title),
                        style = MaterialTheme.typography.displaySmall,
                        color = HavaTheme.sky.content,
                    )
                }
            }
        }
    }
}
