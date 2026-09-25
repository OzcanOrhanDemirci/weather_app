package com.ozcanorhandemirci.hava.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ozcanorhandemirci.hava.core.database.dao.CityDao
import com.ozcanorhandemirci.hava.core.database.dao.ForecastDao
import com.ozcanorhandemirci.hava.core.database.entity.CityEntity
import com.ozcanorhandemirci.hava.core.database.entity.ForecastEntity

/**
 * The store on the device.
 *
 * The schema is exported to the repository so that a future migration can be
 * reviewed as a difference between two committed files rather than taken on
 * trust.
 */
@Database(
    entities = [CityEntity::class, ForecastEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class HavaDatabase : RoomDatabase() {

    abstract fun cityDao(): CityDao

    abstract fun forecastDao(): ForecastDao
}
