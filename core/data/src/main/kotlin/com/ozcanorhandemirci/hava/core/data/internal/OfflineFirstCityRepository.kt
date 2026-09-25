package com.ozcanorhandemirci.hava.core.data.internal

import com.ozcanorhandemirci.hava.core.common.Dispatcher
import com.ozcanorhandemirci.hava.core.common.HavaDispatcher
import com.ozcanorhandemirci.hava.core.data.CityRepository
import com.ozcanorhandemirci.hava.core.database.dao.CityDao
import com.ozcanorhandemirci.hava.core.model.City
import com.ozcanorhandemirci.hava.core.model.Outcome
import com.ozcanorhandemirci.hava.core.network.OpenMeteoDataSource
import java.text.Collator
import java.time.Clock
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
internal class OfflineFirstCityRepository @Inject constructor(
    private val cityDao: CityDao,
    private val remote: OpenMeteoDataSource,
    private val clock: Clock,
    @Dispatcher(HavaDispatcher.IO) private val ioDispatcher: CoroutineDispatcher,
) : CityRepository {

    /**
     * Sorted here rather than in the query.
     *
     * SQLite compares text by code point, which puts every Turkish city whose
     * name begins with a dotted capital after every city whose name does not,
     * and leaves the alphabet the reader expects in pieces. A collator sorts by
     * the rules of the language the device is set to, which is the order a
     * person is actually looking for.
     */
    override fun observeCities(): Flow<List<City>> =
        cityDao.observeAll().map { entities -> entities.map { it.toCity() }.sortedByName() }
            .flowOn(ioDispatcher)

    override fun observeFavorites(): Flow<List<City>> =
        cityDao.observeFavorites().map { entities -> entities.map { it.toCity() } }
            .flowOn(ioDispatcher)

    override fun observeFavoriteIds(): Flow<Set<Long>> =
        cityDao.observeFavorites().map { entities -> entities.mapTo(mutableSetOf()) { it.id } }
            .flowOn(ioDispatcher)

    override fun observeCity(id: Long): Flow<City?> =
        cityDao.observe(id).map { it?.toCity() }.flowOn(ioDispatcher)

    override suspend fun setFavorite(cityId: Long, isFavorite: Boolean) {
        withContext(ioDispatcher) {
            cityDao.setFavorite(cityId, if (isFavorite) clock.millis() else null)
        }
    }

    override suspend fun search(query: String): Outcome<List<City>> {
        val trimmed = query.trim()
        if (trimmed.length < MINIMUM_QUERY_LENGTH) return Outcome.Success(emptyList())

        return remote.search(trimmed, Locale.getDefault().language)
    }

    override suspend fun remember(city: City) {
        withContext(ioDispatcher) {
            // The moment the city was favored is read back and written again,
            // because an upsert of the record from search would otherwise
            // replace it with nothing and quietly drop the city out of the
            // favorites.
            val favoritedAt = cityDao.favoritedAt(city.id)
            cityDao.upsert(listOf(city.toEntity(favoritedAt = favoritedAt)))
        }
    }

    private fun List<City>.sortedByName(): List<City> {
        val collator = Collator.getInstance(Locale.getDefault())
        return sortedWith(compareBy(collator) { it.name })
    }

    private companion object {
        /** A single letter matches most of the country and is not worth asking about. */
        const val MINIMUM_QUERY_LENGTH = 2
    }
}
