package com.ozcanorhandemirci.hava.core.sky

import androidx.compose.ui.graphics.lerp
import com.ozcanorhandemirci.hava.core.designsystem.theme.ColorContrast
import com.ozcanorhandemirci.hava.core.model.Coordinates
import com.ozcanorhandemirci.hava.core.model.WeatherKind
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import java.time.Instant
import org.junit.Test

/**
 * The palette is generated, not authored, so it cannot be approved once by eye.
 * These tests hold it to the two things a generated palette must never lose:
 * legible content, and a night that actually looks like night.
 */
class SkyPaletteFactoryTest {

    /** Every code Open-Meteo publishes. */
    private val everyWeatherCode = listOf(
        0, 1, 2, 3, 45, 48, 51, 53, 55, 56, 57, 61, 63, 65, 66, 67,
        71, 73, 75, 77, 80, 81, 82, 85, 86, 95, 96, 99,
    )

    private val places = mapOf(
        "Izmir" to Coordinates(38.4237, 27.1428),
        "Tromso" to Coordinates(69.6492, 18.9553),
        "Sydney" to Coordinates(-33.8688, 151.2093),
        "Singapore" to Coordinates(1.3521, 103.8198),
    )

    private val dates = listOf("2026-03-20", "2026-06-21", "2026-09-23", "2026-12-21")

    @Test
    fun `content stays legible on its own glass in every sky the application can produce`() {
        val failures = buildList {
            forEverySky { label, palette ->
                val backdrop = lerp(palette.zenith, palette.horizon, CONTENT_HEIGHT)
                val pane = ColorContrast.composite(palette.glass, backdrop)
                val ratio = ColorContrast.ratio(palette.content, pane)

                if (ratio < ColorContrast.MINIMUM_FOR_TEXT) {
                    add("$label produced a contrast ratio of ${"%.2f".format(ratio)}")
                }
            }
        }

        failures.shouldBeEmpty()
    }

    @Test
    fun `the glass never has to become opaque to stay legible`() {
        val failures = buildList {
            forEverySky { label, palette ->
                if (palette.glass.alpha > MAXIMUM_ACCEPTABLE_VEIL) {
                    add("$label needed a veil of ${"%.2f".format(palette.glass.alpha)}")
                }
            }
        }

        failures.shouldBeEmpty()
    }

    @Test
    fun `a clear midnight is darker than a clear midday in the same place`() {
        val midnight = paletteAt("2026-06-21T21:10:00Z", places.getValue("Izmir"), code = 0)
        val midday = paletteAt("2026-06-21T10:10:00Z", places.getValue("Izmir"), code = 0)

        val nightLuminance = ColorContrast.relativeLuminance(midnight.zenith)
        val dayLuminance = ColorContrast.relativeLuminance(midday.zenith)

        (nightLuminance < dayLuminance) shouldBe true
        midnight.isNight shouldBe true
        midday.isNight shouldBe false
    }

    @Test
    fun `an overcast midday is drained of color but stays bright`() {
        val clear = paletteAt("2026-06-21T10:10:00Z", places.getValue("Izmir"), code = 0)
        val overcast = paletteAt("2026-06-21T10:10:00Z", places.getValue("Izmir"), code = 3)

        val clearSaturation = clear.zenith.let { maxOf(it.red, it.green, it.blue) - minOf(it.red, it.green, it.blue) }
        val overcastSaturation =
            overcast.zenith.let { maxOf(it.red, it.green, it.blue) - minOf(it.red, it.green, it.blue) }

        (overcastSaturation < clearSaturation) shouldBe true
        (ColorContrast.relativeLuminance(overcast.zenith) > 0.05) shouldBe true
    }

    @Test
    fun `fog closes the distance between the top and the bottom of the sky`() {
        val clear = paletteAt("2026-06-21T10:10:00Z", places.getValue("Izmir"), code = 0)
        val foggy = paletteAt("2026-06-21T10:10:00Z", places.getValue("Izmir"), code = 45)

        fun spread(top: androidx.compose.ui.graphics.Color, bottom: androidx.compose.ui.graphics.Color) =
            kotlin.math.abs(ColorContrast.relativeLuminance(top) - ColorContrast.relativeLuminance(bottom))

        (spread(foggy.zenith, foggy.horizon) < spread(clear.zenith, clear.horizon)) shouldBe true
    }

    private fun paletteAt(instant: String, coordinates: Coordinates, code: Int) =
        SkyPaletteFactory.create(
            SkyConditions(
                kind = WeatherKind.fromWmoCode(code),
                instant = Instant.parse(instant),
                coordinates = coordinates,
                windSpeedKph = 10.0,
                windDirectionDegrees = 180,
            ),
        )

    private inline fun forEverySky(
        action: (label: String, palette: com.ozcanorhandemirci.hava.core.designsystem.theme.SkyPalette) -> Unit,
    ) {
        for ((placeName, coordinates) in places) {
            for (date in dates) {
                for (hour in 0..23) {
                    val instant = "${date}T%02d:30:00Z".format(hour)
                    for (code in everyWeatherCode) {
                        action(
                            "$placeName on $date at ${"%02d".format(hour)}:30 with code $code",
                            paletteAt(instant, coordinates, code),
                        )
                    }
                }
            }
        }
    }

    private companion object {
        /** Matches the height the factory solves the veil for. */
        const val CONTENT_HEIGHT = 0.45f

        /** Beyond this the pane would stop showing the weather behind it. */
        const val MAXIMUM_ACCEPTABLE_VEIL = 0.80f
    }
}
