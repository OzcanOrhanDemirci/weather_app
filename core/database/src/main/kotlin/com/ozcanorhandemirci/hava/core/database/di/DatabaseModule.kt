package com.ozcanorhandemirci.hava.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ozcanorhandemirci.hava.core.database.HavaDatabase
import com.ozcanorhandemirci.hava.core.database.SeedCities
import com.ozcanorhandemirci.hava.core.database.dao.CityDao
import com.ozcanorhandemirci.hava.core.database.dao.ForecastDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Provides
    @Singleton
    fun providesDatabase(@ApplicationContext context: Context): HavaDatabase =
        Room.databaseBuilder(context, HavaDatabase::class.java, DATABASE_NAME)
            .addCallback(SeedOnCreate)
            .build()

    @Provides
    fun providesCityDao(database: HavaDatabase): CityDao = database.cityDao()

    @Provides
    fun providesForecastDao(database: HavaDatabase): ForecastDao = database.forecastDao()

    private const val DATABASE_NAME = "hava.db"
}

/**
 * Fills the city table the first time the database is created.
 *
 * Done here rather than on first read so that the list is never briefly empty,
 * and written through bound parameters rather than assembled strings so a place
 * name can never be mistaken for part of the statement.
 */
private object SeedOnCreate : RoomDatabase.Callback() {

    private const val INSERT = "INSERT INTO cities " +
        "(id, name, region, country_code, latitude, longitude, time_zone_id, favorited_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, NULL)"

    override fun onCreate(db: SupportSQLiteDatabase) {
        SeedCities.forEach { city ->
            db.execSQL(
                INSERT,
                arrayOf<Any?>(
                    city.id,
                    city.name,
                    city.region,
                    city.countryCode,
                    city.latitude,
                    city.longitude,
                    city.timeZoneId,
                ),
            )
        }
    }
}
