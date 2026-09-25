package com.ozcanorhandemirci.hava.core.data.testing

import com.ozcanorhandemirci.hava.core.data.CityRepository
import com.ozcanorhandemirci.hava.core.data.WeatherRepository
import com.ozcanorhandemirci.hava.core.data.model.CachedForecast
import com.ozcanorhandemirci.hava.core.model.City
import com.ozcanorhandemirci.hava.core.model.Outcome
import com.ozcanorhandemirci.hava.core.model.WeatherError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * Repositories a test can drive.
 *
 * They live in the main source set rather than in a test one so that the
 * feature modules can use them. A feature depends on the repository interfaces,
 * and the alternative is for each feature to write its own stand in and for the
 * four of them to disagree about what the contract is.
 *
 * Nothing in the application wires these up; the only way to reach one is to
 * construct it.
 */
class FakeCityRepository : CityRepository {

    private val cities = MutableStateFlow(emptyList<City>())
    private val favorites = MutableStateFlow(emptySet<Long>())

    var searchResult: Outcome<List<City>> = Outcome.Success(emptyList())

    var remembered: City? = null
        private set

    /** How many times the service was actually asked, which is what a debounce is for. */
    var searchCount: Int = 0
        private set

    fun setCities(value: List<City>) {
        cities.value = value
    }

    fun setFavorites(ids: Set<Long>) {
        favorites.value = ids
    }

    override fun observeCities(): Flow<List<City>> = cities.asStateFlow()

    override fun observeFavorites(): Flow<List<City>> =
        cities.map { all -> all.filter { it.id in favorites.value } }

    override fun observeFavoriteIds(): Flow<Set<Long>> = favorites.asStateFlow()

    override fun observeCity(id: Long): Flow<City?> =
        cities.map { all -> all.firstOrNull { it.id == id } }

    override suspend fun setFavorite(cityId: Long, isFavorite: Boolean) {
        favorites.value = if (isFavorite) favorites.value + cityId else favorites.value - cityId
    }

    override suspend fun search(query: String): Outcome<List<City>> {
        searchCount++
        return searchResult
    }

    override suspend fun remember(city: City) {
        remembered = city
        cities.value = cities.value.filterNot { it.id == city.id } + city
    }
}

class FakeWeatherRepository : WeatherRepository {

    private val forecasts = MutableStateFlow(emptyMap<Long, CachedForecast>())

    /** What the next refresh will do. */
    var refreshResult: Outcome<Unit> = Outcome.Success(Unit)

    var lastRefreshWasForced: Boolean? = null
        private set

    fun setForecasts(value: Map<Long, CachedForecast>) {
        forecasts.value = value
    }

    override fun observeForecast(cityId: Long): Flow<CachedForecast?> =
        forecasts.map { it[cityId] }

    override fun observeForecasts(): Flow<Map<Long, CachedForecast>> = forecasts.asStateFlow()

    override suspend fun refresh(city: City): Outcome<Unit> = refreshResult

    override suspend fun refresh(cities: List<City>, force: Boolean): Outcome<Unit> {
        lastRefreshWasForced = force
        return refreshResult
    }
}

/** A failure that is easy to assert on. */
val TestFailure: WeatherError = WeatherError.Offline
