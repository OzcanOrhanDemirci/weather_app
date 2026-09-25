package com.ozcanorhandemirci.hava.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ozcanorhandemirci.hava.core.database.entity.CityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CityDao {

    @Query("SELECT * FROM cities")
    fun observeAll(): Flow<List<CityEntity>>

    @Query("SELECT * FROM cities WHERE favorited_at IS NOT NULL ORDER BY favorited_at ASC")
    fun observeFavorites(): Flow<List<CityEntity>>

    @Query("SELECT * FROM cities WHERE id = :id")
    fun observe(id: Long): Flow<CityEntity?>

    @Query("SELECT favorited_at FROM cities WHERE id = :id")
    suspend fun favoritedAt(id: Long): Long?

    @Upsert
    suspend fun upsert(cities: List<CityEntity>)

    /** Passing null removes the city from the favorites. */
    @Query("UPDATE cities SET favorited_at = :favoritedAt WHERE id = :id")
    suspend fun setFavorite(id: Long, favoritedAt: Long?)
}
