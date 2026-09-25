package com.ozcanorhandemirci.hava.core.data.internal

import com.ozcanorhandemirci.hava.core.common.Dispatcher
import com.ozcanorhandemirci.hava.core.common.HavaDispatcher
import com.ozcanorhandemirci.hava.core.data.WeatherRepository
import com.ozcanorhandemirci.hava.core.data.model.CachedForecast
import com.ozcanorhandemirci.hava.core.database.dao.CityDao
import com.ozcanorhandemirci.hava.core.database.dao.ForecastDao
import com.ozcanorhandemirci.hava.core.database.entity.ForecastEntity
import com.ozcanorhandemirci.hava.core.model.City
import com.ozcanorhandemirci.hava.core.model.Outcome
import com.ozcanorhandemirci.hava.core.model.WeatherError
import com.ozcanorhandemirci.hava.core.network.ForecastDocument
import com.ozcanorhandemirci.hava.core.network.ForecastReader
import com.ozcanorhandemirci.hava.core.network.OpenMeteoDataSource
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

@Singleton
internal class OfflineFirstWeatherRepository @Inject constructor(
    private val cityDao: CityDao,
    private val forecastDao: ForecastDao,
    private val remote: OpenMeteoDataSource,
    private val reader: ForecastReader,
    private val clock: Clock,
    @Dispatcher(HavaDispatcher.IO) private val ioDispatcher: CoroutineDispatcher,
) : WeatherRepository {

    /**
     * Forecasts that have already been turned into the domain, kept by city.
     *
     * The stored document is several kilobytes of text and the city list reads
     * every one of them whenever anything in the database changes, including a
     * favorite being toggled. Parsing is skipped when the stored row has not
     * moved, which is nearly always.
     */
    private val parsed = ConcurrentHashMap<Long, ParsedForecast>()

    override fun observeForecast(cityId: Long): Flow<CachedForecast?> =
        combine(cityDao.observe(cityId), forecastDao.observe(cityId)) { city, forecast ->
            if (city == null || forecast == null) null else forecast.toCached(city.toCity())
        }.flowOn(ioDispatcher)

    override fun observeForecasts(): Flow<Map<Long, CachedForecast>> =
        combine(cityDao.observeAll(), forecastDao.observeAll()) { cities, forecasts ->
            val byId = cities.associateBy { it.id }
            forecasts.mapNotNull { forecast ->
                val city = byId[forecast.cityId] ?: return@mapNotNull null
                forecast.toCached(city.toCity())?.let { forecast.cityId to it }
            }.toMap()
        }.flowOn(ioDispatcher)

    override suspend fun refresh(city: City): Outcome<Unit> =
        when (val document = remote.forecast(city.coordinates)) {
            is Outcome.Failure -> document
            is Outcome.Success -> {
                store(city, document.value)
                Outcome.Success(Unit)
            }
        }

    override suspend fun refreshStale(cities: List<City>): Outcome<Unit> {
        val now = clock.instant()
        val storedAt = withContext(ioDispatcher) { forecastDao.retrievalTimes() }
            .associate { it.cityId to Instant.ofEpochMilli(it.retrievedAt) }

        val due = cities.filter { city ->
            val retrievedAt = storedAt[city.id]
            retrievedAt == null || Duration.between(retrievedAt, now) > CachedForecast.MAX_AGE
        }

        if (due.isEmpty()) return Outcome.Success(Unit)

        // Twenty cities at once would open twenty connections and finish no
        // sooner, because the limit is the round trip and not this device.
        val gate = Semaphore(MAX_PARALLEL_REQUESTS)
        val results = coroutineScope {
            due.map { city -> async { gate.withPermit { refresh(city) } } }.awaitAll()
        }

        val firstFailure = results.filterIsInstance<Outcome.Failure>().firstOrNull()
        return when {
            results.any { it is Outcome.Success } -> Outcome.Success(Unit)
            firstFailure != null -> firstFailure
            else -> Outcome.Failure(WeatherError.Offline)
        }
    }

    private suspend fun store(city: City, document: ForecastDocument) = withContext(ioDispatcher) {
        val retrievedAt = clock.instant()
        parsed.remove(city.id)
        forecastDao.upsert(
            ForecastEntity(
                cityId = city.id,
                retrievedAt = retrievedAt.toEpochMilli(),
                document = document.json,
            ),
        )
    }

    private fun ForecastEntity.toCached(city: City): CachedForecast? {
        val memo = parsed[cityId]
        if (memo != null && memo.retrievedAt == retrievedAt) return memo.value

        val retrieved = Instant.ofEpochMilli(retrievedAt)
        val snapshot = reader.read(ForecastDocument(document), city, retrieved).valueOrNull
            ?: return null

        val cached = CachedForecast(snapshot = snapshot, retrievedAt = retrieved)
        parsed[cityId] = ParsedForecast(retrievedAt = retrievedAt, value = cached)
        return cached
    }

    private data class ParsedForecast(val retrievedAt: Long, val value: CachedForecast)

    private companion object {
        const val MAX_PARALLEL_REQUESTS = 6
    }
}
