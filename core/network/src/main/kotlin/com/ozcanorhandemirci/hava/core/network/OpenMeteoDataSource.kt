package com.ozcanorhandemirci.hava.core.network

import com.ozcanorhandemirci.hava.core.common.Dispatcher
import com.ozcanorhandemirci.hava.core.common.HavaDispatcher
import com.ozcanorhandemirci.hava.core.model.City
import com.ozcanorhandemirci.hava.core.model.Coordinates
import com.ozcanorhandemirci.hava.core.model.Outcome
import com.ozcanorhandemirci.hava.core.model.WeatherError
import com.ozcanorhandemirci.hava.core.network.dto.GeocodingDto
import com.ozcanorhandemirci.hava.core.network.dto.PlaceDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import java.io.IOException
import java.nio.channels.UnresolvedAddressException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException

/** Every call this application makes to the outside world. */
@Singleton
class OpenMeteoDataSource @Inject constructor(
    private val client: HttpClient,
    @Dispatcher(HavaDispatcher.IO) private val ioDispatcher: CoroutineDispatcher,
) {

    /** Fetches a forecast verbatim, so the same bytes can be stored and reused. */
    suspend fun forecast(coordinates: Coordinates): Outcome<ForecastDocument> = request {
        val body: String = client.get(OpenMeteo.FORECAST_URL) {
            parameter("latitude", coordinates.latitude)
            parameter("longitude", coordinates.longitude)
            parameter("timezone", "auto")
            parameter("timeformat", OpenMeteo.TIME_FORMAT)
            parameter("forecast_days", OpenMeteo.FORECAST_DAYS)
            parameter("current", OpenMeteo.CURRENT_FIELDS.joinToString(","))
            parameter("hourly", OpenMeteo.HOURLY_FIELDS.joinToString(","))
            parameter("daily", OpenMeteo.DAILY_FIELDS.joinToString(","))
        }.body()

        ForecastDocument(body)
    }

    /**
     * Searches for a place by name.
     *
     * The language tag is passed on, so a device set to Turkish is offered
     * Kopenhag rather than Copenhagen.
     */
    suspend fun search(query: String, languageTag: String): Outcome<List<City>> = request {
        val response: GeocodingDto = client.get(OpenMeteo.GEOCODING_URL) {
            parameter("name", query)
            parameter("count", SEARCH_RESULT_LIMIT)
            parameter("language", languageTag)
            parameter("format", "json")
        }.body()

        response.results.orEmpty().map(PlaceDto::toCity)
    }

    /**
     * Runs a call and turns the ways it can fail into reasons the interface can
     * act on.
     *
     * Cancellation is rethrown rather than reported. A screen that has gone
     * away has not encountered an error, and turning its cancellation into one
     * would record a failure against a request nobody is waiting for.
     */
    private suspend fun <T> request(block: suspend () -> T): Outcome<T> = withContext(ioDispatcher) {
        try {
            Outcome.Success(block())
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (unresolved: UnresolvedAddressException) {
            Outcome.Failure(WeatherError.Offline)
        } catch (timeout: HttpRequestTimeoutException) {
            Outcome.Failure(WeatherError.Timeout)
        } catch (timeout: ConnectTimeoutException) {
            Outcome.Failure(WeatherError.Timeout)
        } catch (timeout: SocketTimeoutException) {
            Outcome.Failure(WeatherError.Timeout)
        } catch (rejected: ResponseException) {
            Outcome.Failure(WeatherError.Service(rejected.response.status.value))
        } catch (unreadable: SerializationException) {
            Outcome.Failure(WeatherError.Unreadable)
        } catch (connection: IOException) {
            Outcome.Failure(WeatherError.Offline)
        }
    }

    private companion object {
        const val SEARCH_RESULT_LIMIT = 12
    }
}

private fun PlaceDto.toCity() = City(
    id = id,
    name = name,
    region = admin1,
    countryCode = countryCode.orEmpty(),
    coordinates = Coordinates(latitude = latitude, longitude = longitude),
    timeZoneId = timezone,
)
