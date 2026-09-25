package com.ozcanorhandemirci.hava.feature.detail

import kotlinx.serialization.Serializable

/**
 * Where this screen lives.
 *
 * Declared as a type rather than as a string with a placeholder in it, so the
 * identifier cannot be spelled one way when the route is built and another way
 * when it is read.
 */
@Serializable
data class CityDetailDestination(val cityId: Long)
