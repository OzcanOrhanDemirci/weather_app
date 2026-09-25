package com.ozcanorhandemirci.hava.core.data.internal

import com.ozcanorhandemirci.hava.core.database.entity.CityEntity
import com.ozcanorhandemirci.hava.core.model.City
import com.ozcanorhandemirci.hava.core.model.Coordinates

internal fun CityEntity.toCity() = City(
    id = id,
    name = name,
    region = region,
    countryCode = countryCode,
    coordinates = Coordinates(latitude = latitude, longitude = longitude),
    timeZoneId = timeZoneId,
)

internal fun City.toEntity(favoritedAt: Long? = null) = CityEntity(
    id = id,
    name = name,
    region = region,
    countryCode = countryCode,
    latitude = coordinates.latitude,
    longitude = coordinates.longitude,
    timeZoneId = timeZoneId,
    favoritedAt = favoritedAt,
)
