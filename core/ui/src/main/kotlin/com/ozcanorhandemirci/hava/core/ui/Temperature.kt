package com.ozcanorhandemirci.hava.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import com.ozcanorhandemirci.hava.core.model.Temperature
import com.ozcanorhandemirci.hava.core.model.TemperatureUnit

/**
 * A temperature as it is read aloud.
 *
 * Rounded once, here, so that the same reading cannot appear as 21 in a list
 * and 22 on the screen it opens.
 */
@Composable
@ReadOnlyComposable
fun Temperature.format(unit: TemperatureUnit = TemperatureUnit.CELSIUS): String =
    stringResource(R.string.temperature_degrees, roundedTo(unit))
