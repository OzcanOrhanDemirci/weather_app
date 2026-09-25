package com.ozcanorhandemirci.hava.core.data

import com.ozcanorhandemirci.hava.core.model.City
import com.ozcanorhandemirci.hava.core.model.Outcome
import kotlinx.coroutines.flow.Flow

/** The places the application knows about. */
interface CityRepository {

    fun observeCities(): Flow<List<City>>

    fun observeFavorites(): Flow<List<City>>

    fun observeFavoriteIds(): Flow<Set<Long>>

    fun observeCity(id: Long): Flow<City?>

    suspend fun setFavorite(cityId: Long, isFavorite: Boolean)

    /** Looks a place up by name. Results are not stored until one is opened. */
    suspend fun search(query: String): Outcome<List<City>>

    /** Stores a place found through search, so it can be favored and cached. */
    suspend fun remember(city: City)
}
