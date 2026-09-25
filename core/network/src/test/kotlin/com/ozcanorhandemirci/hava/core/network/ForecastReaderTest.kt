package com.ozcanorhandemirci.hava.core.network

import com.ozcanorhandemirci.hava.core.model.City
import com.ozcanorhandemirci.hava.core.model.Coordinates
import com.ozcanorhandemirci.hava.core.model.Outcome
import com.ozcanorhandemirci.hava.core.model.WeatherCondition
import com.ozcanorhandemirci.hava.core.model.WeatherError
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.time.Instant
import java.time.LocalDate
import kotlinx.serialization.json.Json
import org.junit.Test

/**
 * Read against a response captured from the live service rather than one
 * written by hand. A handwritten fixture only ever tests that the parser agrees
 * with whoever wrote the fixture.
 */
class ForecastReaderTest {

    private val reader = ForecastReader(Json { ignoreUnknownKeys = true; explicitNulls = false })

    private val izmir = City(
        id = 311046L,
        name = "İzmir",
        region = "İzmir",
        countryCode = "TR",
        coordinates = Coordinates(38.4127, 27.1384),
        timeZoneId = "Europe/Istanbul",
    )

    private val retrievedAt = Instant.parse("2026-09-25T08:00:00Z")

    private fun readFixture(): Outcome<com.ozcanorhandemirci.hava.core.model.WeatherSnapshot> {
        val json = checkNotNull(javaClass.getResourceAsStream("/izmir-forecast.json"))
            .bufferedReader()
            .use { it.readText() }
        return reader.read(ForecastDocument(json), izmir, retrievedAt)
    }

    private fun snapshot() = (readFixture() as Outcome.Success).value

    @Test
    fun `a live response produces a full week of hours and days`() {
        val snapshot = snapshot()

        snapshot.hourly.size shouldBe 168
        snapshot.daily.size shouldBe 7
        snapshot.city shouldBe izmir
        snapshot.retrievedAt shouldBe retrievedAt
    }

    @Test
    fun `the current conditions carry through`() {
        val current = snapshot().current

        current.temperature.celsius shouldBe 22.2
        current.kind.condition shouldBe WeatherCondition.CLEAR
        current.isDay shouldBe true
        current.relativeHumidityPercent shouldBe 36
    }

    /**
     * The service marks a day with the instant that day begins where the city
     * is. Read as though it were in universal time, the first day of this
     * response lands on 24 September, while in Izmir it is the 25th. Every date
     * on the daily strip would be a day early, and only for cities east of
     * Greenwich.
     */
    @Test
    fun `a day is dated in the zone of the city and not in universal time`() {
        val firstDay = snapshot().daily.first()

        firstDay.date shouldBe LocalDate.of(2026, 9, 25)
    }

    @Test
    fun `sunrise comes before sunset on every day of the week`() {
        snapshot().daily.forEach { day ->
            (day.sunrise < day.sunset) shouldBe true
        }
    }

    @Test
    fun `hours arrive in order`() {
        val hours = snapshot().hourly

        hours.zipWithNext().all { (earlier, later) -> earlier.time < later.time } shouldBe true
    }

    @Test
    fun `a response that cannot be read is reported rather than thrown`() {
        val outcome = reader.read(ForecastDocument("{ this is not json"), izmir, retrievedAt)

        outcome.shouldBeInstanceOf<Outcome.Failure>()
        outcome.reason shouldBe WeatherError.Unreadable
    }

    @Test
    fun `a response missing a required block is reported rather than thrown`() {
        val outcome = reader.read(ForecastDocument("""{"latitude":1.0}"""), izmir, retrievedAt)

        outcome.shouldBeInstanceOf<Outcome.Failure>()
        outcome.reason shouldBe WeatherError.Unreadable
    }
}
