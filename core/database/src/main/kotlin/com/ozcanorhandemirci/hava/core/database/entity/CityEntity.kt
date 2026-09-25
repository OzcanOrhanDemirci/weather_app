package com.ozcanorhandemirci.hava.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A place stored on the device.
 *
 * The identifier is the one the geocoding service issues, not one generated
 * here. That is what stops a city found through search from being stored a
 * second time next to the copy that shipped with the application.
 *
 * Favoring is held as the moment it happened rather than as a flag, which
 * costs the same and gives the favorites list its order for nothing.
 */
@Entity(tableName = "cities")
data class CityEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val region: String?,
    @ColumnInfo(name = "country_code") val countryCode: String,
    val latitude: Double,
    val longitude: Double,
    @ColumnInfo(name = "time_zone_id") val timeZoneId: String,
    @ColumnInfo(name = "favorited_at", index = true) val favoritedAt: Long?,
)
