package com.ozcanorhandemirci.hava.feature.cities

import app.cash.turbine.test
import com.ozcanorhandemirci.hava.core.common.testing.MainDispatcherRule
import com.ozcanorhandemirci.hava.core.data.testing.FakeCityRepository
import com.ozcanorhandemirci.hava.core.data.testing.FakeWeatherRepository
import com.ozcanorhandemirci.hava.core.data.testing.Samples
import com.ozcanorhandemirci.hava.core.model.Outcome
import com.ozcanorhandemirci.hava.core.model.WeatherError
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class CitiesViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val cities = FakeCityRepository()
    private val weather = FakeWeatherRepository()

    private fun viewModel() = CitiesViewModel(cities, weather)

    @Test
    fun `with no cities and no failure the list is empty rather than broken`() = runTest {
        viewModel().uiState.test {
            expectMostRecentItem() shouldBe CitiesUiState.Empty
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a city with no forecast yet is still listed`() = runTest {
        cities.setCities(listOf(Samples.izmir))

        viewModel().uiState.test {
            val content = expectMostRecentItem().shouldBeInstanceOf<CitiesUiState.Content>()
            content.cities.single().city shouldBe Samples.izmir
            content.cities.single().snapshot shouldBe null
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a forecast is attached to its city`() = runTest {
        cities.setCities(listOf(Samples.izmir, Samples.rize))
        weather.setForecasts(mapOf(Samples.rize.id to Samples.cached(Samples.rize, celsius = 14.0)))

        viewModel().uiState.test {
            val content = expectMostRecentItem().shouldBeInstanceOf<CitiesUiState.Content>()
            content.cities.first { it.city.id == Samples.rize.id }
                .snapshot?.current?.temperature?.celsius shouldBe 14.0
            content.cities.first { it.city.id == Samples.izmir.id }.snapshot shouldBe null
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * The behaviour the whole offline design exists for. A refresh that fails
     * while the device already holds weather must not replace that weather with
     * an error; it reports the problem beside it.
     */
    @Test
    fun `a failed refresh keeps the content and reports the problem beside it`() = runTest {
        cities.setCities(listOf(Samples.izmir))
        weather.setForecasts(mapOf(Samples.izmir.id to Samples.cached(Samples.izmir)))
        weather.refreshResult = Outcome.Failure(WeatherError.Offline)

        viewModel().uiState.test {
            val settled = expectMostRecentItem().shouldBeInstanceOf<CitiesUiState.Content>()
            settled.cities.single().snapshot shouldBe Samples.snapshot(Samples.izmir)
            settled.problem shouldBe WeatherError.Offline
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Only when there is nothing to show at all does a failure take over the
     * screen.
     */
    @Test
    fun `a failure with nothing stored is the whole screen`() = runTest {
        weather.refreshResult = Outcome.Failure(WeatherError.Timeout)

        viewModel().uiState.test {
            expectMostRecentItem() shouldBe CitiesUiState.Failed(WeatherError.Timeout)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `the fetch on opening spares the cities that are still current`() = runTest {
        cities.setCities(listOf(Samples.izmir))

        viewModel().uiState.test {
            expectMostRecentItem()
            weather.lastRefreshWasForced shouldBe false
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `pulling the list down asks again for every city`() = runTest {
        cities.setCities(listOf(Samples.izmir))
        val model = viewModel()

        model.uiState.test {
            expectMostRecentItem()
            model.refresh()
            weather.lastRefreshWasForced shouldBe true
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a successful refresh clears a problem that was reported before`() = runTest {
        cities.setCities(listOf(Samples.izmir))
        weather.setForecasts(mapOf(Samples.izmir.id to Samples.cached(Samples.izmir)))
        weather.refreshResult = Outcome.Failure(WeatherError.Offline)

        val model = viewModel()
        model.uiState.test {
            expectMostRecentItem().shouldBeInstanceOf<CitiesUiState.Content>().problem shouldBe
                WeatherError.Offline

            weather.refreshResult = Outcome.Success(Unit)
            model.refresh()

            expectMostRecentItem().shouldBeInstanceOf<CitiesUiState.Content>().problem shouldBe null
            cancelAndIgnoreRemainingEvents()
        }
    }
}
