package com.ozcanorhandemirci.hava.feature.search

import app.cash.turbine.test
import com.ozcanorhandemirci.hava.core.common.testing.MainDispatcherRule
import com.ozcanorhandemirci.hava.core.data.testing.FakeCityRepository
import com.ozcanorhandemirci.hava.core.data.testing.Samples
import com.ozcanorhandemirci.hava.core.model.Outcome
import com.ozcanorhandemirci.hava.core.model.WeatherError
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

/**
 * The assertions are about where the screen settles and how many times the
 * service was asked, not about the states passed through on the way. Which
 * intermediate emissions a collector observes depends on how the test
 * dispatcher schedules them, and asserting on that would be testing the test
 * framework.
 */
class SearchViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val cities = FakeCityRepository()

    private fun viewModel() = SearchViewModel(cities)

    @Test
    fun `one letter is not a search`() = runTest {
        val model = viewModel()

        model.uiState.test {
            awaitItem() shouldBe SearchUiState.Idle

            model.onQueryChange("i")
            advanceTimeBy(SETTLE * 2)

            // Nothing new is published and nothing was asked of the service.
            expectNoEvents()
            cities.searchCount shouldBe 0
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a match becomes a list of places`() = runTest {
        cities.searchResult = Outcome.Success(listOf(Samples.izmir))
        val model = viewModel()

        model.uiState.test {
            model.onQueryChange("izmir")
            advanceTimeBy(SETTLE * 2)

            expectMostRecentItem().shouldBeInstanceOf<SearchUiState.Results>()
                .places.single() shouldBe Samples.izmir
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * An empty answer is the most common outcome while a query is still being
     * typed. Reporting it as a failure would put an error in front of ordinary
     * typing.
     */
    @Test
    fun `no match is its own state and not a failure`() = runTest {
        cities.searchResult = Outcome.Success(emptyList())
        val model = viewModel()

        model.uiState.test {
            model.onQueryChange("zzzqqq")
            advanceTimeBy(SETTLE * 2)

            expectMostRecentItem() shouldBe SearchUiState.NoMatches
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a lost connection is reported as one`() = runTest {
        cities.searchResult = Outcome.Failure(WeatherError.Offline)
        val model = viewModel()

        model.uiState.test {
            model.onQueryChange("izmir")
            advanceTimeBy(SETTLE * 2)

            expectMostRecentItem() shouldBe SearchUiState.Failed(WeatherError.Offline)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Typing quickly must produce one search, not one per keystroke. Without
     * this the service is asked once per letter and the answers arrive out of
     * order.
     */
    @Test
    fun `typing quickly asks the service once`() = runTest {
        cities.searchResult = Outcome.Success(listOf(Samples.rize))
        val model = viewModel()

        model.uiState.test {
            "rize".forEachIndexed { index, _ ->
                model.onQueryChange("rize".take(index + 1))
                advanceTimeBy(SETTLE / 4)
            }
            advanceTimeBy(SETTLE * 2)

            expectMostRecentItem().shouldBeInstanceOf<SearchUiState.Results>()
            cities.searchCount shouldBe 1
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearing the field returns to the resting state`() = runTest {
        cities.searchResult = Outcome.Success(listOf(Samples.izmir))
        val model = viewModel()

        model.uiState.test {
            model.onQueryChange("izmir")
            advanceTimeBy(SETTLE * 2)
            expectMostRecentItem().shouldBeInstanceOf<SearchUiState.Results>()

            model.onClear()
            advanceTimeBy(SETTLE * 2)

            expectMostRecentItem() shouldBe SearchUiState.Idle
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Search results are not stored as they arrive. Typing three letters would
     * otherwise leave a dozen places on the device that nobody asked for.
     */
    @Test
    fun `a place is stored only when it is opened`() = runTest {
        cities.searchResult = Outcome.Success(listOf(Samples.rize))
        val model = viewModel()

        model.uiState.test {
            model.onQueryChange("rize")
            advanceTimeBy(SETTLE * 2)
            expectMostRecentItem().shouldBeInstanceOf<SearchUiState.Results>()

            cities.remembered shouldBe null

            var opened: Long? = null
            model.remember(Samples.rize) { opened = it }

            cities.remembered shouldBe Samples.rize
            opened shouldBe Samples.rize.id
            cancelAndIgnoreRemainingEvents()
        }
    }

    private companion object {
        /** Mirrors the settling delay inside the view model. */
        const val SETTLE = 300L
    }
}
