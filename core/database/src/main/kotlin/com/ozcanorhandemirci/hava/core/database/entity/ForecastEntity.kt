package com.ozcanorhandemirci.hava.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * The last forecast received for a city, stored exactly as the service sent it.
 *
 * Storing the response rather than a set of normalized rows is deliberate. A
 * forecast is read as a whole and replaced as a whole; it is never queried by
 * hour or joined against anything. Splitting it across three tables would buy
 * nothing and would add a second way to turn the wire format into the domain,
 * which is a second way for the two to disagree.
 *
 * The row is removed with its city, so a place that is forgotten does not leave
 * its weather behind.
 */
@Entity(
    tableName = "forecasts",
    foreignKeys = [
        ForeignKey(
            entity = CityEntity::class,
            parentColumns = ["id"],
            childColumns = ["city_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class ForecastEntity(
    @PrimaryKey @ColumnInfo(name = "city_id") val cityId: Long,
    @ColumnInfo(name = "retrieved_at") val retrievedAt: Long,
    val document: String,
)
