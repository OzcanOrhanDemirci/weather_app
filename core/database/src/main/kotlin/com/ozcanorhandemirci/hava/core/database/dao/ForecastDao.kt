package com.ozcanorhandemirci.hava.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ozcanorhandemirci.hava.core.database.entity.ForecastEntity
import kotlinx.coroutines.flow.Flow

/** When a stored forecast arrived, for a city. */
data class ForecastAge(val cityId: Long, val retrievedAt: Long)

@Dao
interface ForecastDao {

    @Query("SELECT * FROM forecasts")
    fun observeAll(): Flow<List<ForecastEntity>>

    @Query("SELECT * FROM forecasts WHERE city_id = :cityId")
    fun observe(cityId: Long): Flow<ForecastEntity?>

    /**
     * When each stored forecast arrived, without its document.
     *
     * Deciding which cities are due for a refresh needs nothing but the age,
     * and reading twenty full responses to find out would be several hundred
     * kilobytes for two columns.
     */
    @Query("SELECT city_id AS cityId, retrieved_at AS retrievedAt FROM forecasts")
    suspend fun retrievalTimes(): List<ForecastAge>

    @Upsert
    suspend fun upsert(forecast: ForecastEntity)

    @Query("DELETE FROM forecasts WHERE retrieved_at < :retrievedBefore")
    suspend fun deleteOlderThan(retrievedBefore: Long)
}
