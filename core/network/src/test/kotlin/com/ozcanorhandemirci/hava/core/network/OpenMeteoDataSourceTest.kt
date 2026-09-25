package com.ozcanorhandemirci.hava.core.network

import com.ozcanorhandemirci.hava.core.model.Coordinates
import com.ozcanorhandemirci.hava.core.model.Outcome
import com.ozcanorhandemirci.hava.core.model.WeatherError
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import java.io.IOException
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Test

class OpenMeteoDataSourceTest {

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    private fun dataSource(engine: MockEngine) = OpenMeteoDataSource(
        client = HttpClient(engine) {
            expectSuccess = true
            install(ContentNegotiation) { json(json) }
        },
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    private fun respondingWith(body: String, status: HttpStatusCode = HttpStatusCode.OK) =
        MockEngine {
            respond(
                content = body,
                status = status,
                headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
            )
        }

    /**
     * A search that matches nothing omits the results field entirely rather
     * than returning an empty array. Treating that as a parse failure would put
     * an error screen in front of every search that simply found nothing, which
     * is the most common outcome while a query is still being typed.
     */
    @Test
    fun `a search that matches nothing is an empty list and not a failure`() = runTest {
        val source = dataSource(respondingWith("""{"generationtime_ms":0.14}"""))

        val outcome = source.search("zzzqqq", languageTag = "tr")

        outcome.shouldBeInstanceOf<Outcome.Success<*>>()
        (outcome.value as List<*>).shouldBeEmpty()
    }

    @Test
    fun `a search result becomes a city`() = runTest {
        val body = """
            {"results":[{"id":311046,"name":"İzmir","latitude":38.41,"longitude":27.13,
            "timezone":"Europe/Istanbul","country_code":"TR","admin1":"İzmir"}]}
        """.trimIndent()
        val source = dataSource(respondingWith(body))

        val outcome = source.search("izmir", languageTag = "tr")

        val cities = (outcome as Outcome.Success).value
        cities.single().name shouldBe "İzmir"
        cities.single().region shouldBe "İzmir"
        cities.single().timeZoneId shouldBe "Europe/Istanbul"
    }

    @Test
    fun `a rejection carries its status code`() = runTest {
        val source = dataSource(MockEngine { respondError(HttpStatusCode.TooManyRequests) })

        val outcome = source.forecast(Coordinates(38.4, 27.1))

        outcome.shouldBeInstanceOf<Outcome.Failure>()
        outcome.reason shouldBe WeatherError.Service(HttpStatusCode.TooManyRequests.value)
    }

    @Test
    fun `a connection that never opens reads as being offline`() = runTest {
        val source = dataSource(MockEngine { throw IOException("no route to host") })

        val outcome = source.forecast(Coordinates(38.4, 27.1))

        outcome.shouldBeInstanceOf<Outcome.Failure>()
        outcome.reason shouldBe WeatherError.Offline
    }
}
