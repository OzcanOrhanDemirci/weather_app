package com.ozcanorhandemirci.hava.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ozcanorhandemirci.hava.core.designsystem.component.GlassSurface
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaLayout
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaSpacing
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaTheme
import com.ozcanorhandemirci.hava.core.model.City
import com.ozcanorhandemirci.hava.core.ui.headlineResource

@Composable
fun SearchRoute(
    onCityOpened: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()

    SearchScreen(
        query = query,
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onPlaceSelected = { city -> viewModel.remember(city, onCityOpened) },
        modifier = modifier,
    )
}

/**
 * Somewhere to type a place name.
 *
 * The column stops widening before the window does, and sits in the middle of
 * whatever is left. A search field drawn across a landscape screen is mostly
 * empty, and a result three words long stranded in the centre of it is harder
 * to read than the same result in a column the width of a phone.
 */
@Composable
internal fun SearchScreen(
    query: String,
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onPlaceSelected: (City) -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val palette = HavaTheme.sky

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = HavaLayout.readableWidth)
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = HavaSpacing.gutter),
            verticalArrangement = Arrangement.spacedBy(HavaSpacing.medium),
        ) {
            Text(
                text = stringResource(R.string.search_title),
                style = MaterialTheme.typography.displaySmall,
                color = palette.content,
                modifier = Modifier.padding(top = HavaSpacing.large),
            )

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = {
                    Text(text = stringResource(R.string.search_placeholder), color = palette.contentMuted)
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                shape = MaterialTheme.shapes.large,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = palette.content,
                    unfocusedTextColor = palette.content,
                    cursorColor = palette.accent,
                    focusedBorderColor = palette.accent,
                    unfocusedBorderColor = palette.glassEdge,
                    focusedContainerColor = palette.glass,
                    unfocusedContainerColor = palette.glass,
                ),
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when (state) {
                    SearchUiState.Idle -> Hint(stringResource(R.string.search_hint))
                    SearchUiState.Searching -> CircularProgressIndicator(
                        color = palette.content,
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = HavaSpacing.section),
                    )

                    SearchUiState.NoMatches -> Hint(stringResource(R.string.search_no_matches, query.trim()))
                    is SearchUiState.Failed -> Hint(stringResource(state.reason.headlineResource()))

                    is SearchUiState.Results -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = HavaLayout.navigationInset),
                        verticalArrangement = Arrangement.spacedBy(HavaSpacing.small),
                    ) {
                        items(items = state.places, key = { it.id }) { place ->
                            PlaceRow(place = place, onClick = { onPlaceSelected(place) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceRow(place: City, onClick: () -> Unit) {
    val palette = HavaTheme.sky
    val where = listOfNotNull(place.region.takeIf { it != place.name }, place.countryCode)
        .joinToString(" · ")

    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = place.name, style = MaterialTheme.typography.titleMedium, color = palette.content)
            if (where.isNotBlank()) {
                Text(text = where, style = MaterialTheme.typography.bodyMedium, color = palette.contentMuted)
            }
        }
    }
}

@Composable
private fun BoxScope.Hint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = HavaTheme.sky.contentMuted,
        textAlign = TextAlign.Center,
        modifier = Modifier.align(Alignment.TopCenter).padding(top = HavaSpacing.section),
    )
}
