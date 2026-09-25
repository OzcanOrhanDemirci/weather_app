package com.ozcanorhandemirci.hava.core.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import javax.inject.Singleton
import kotlinx.serialization.json.Json

/**
 * The network stack.
 *
 * The engine is the platform one rather than OkHttp. This application makes two
 * kinds of request, both of them plain reads, so none of what OkHttp adds is
 * needed here; and the current OkHttp release requires an Android SDK that has
 * not been published, which would make the project impossible to build outside
 * a preview environment. The platform engine also uses the trust store of the
 * device, which is the one the device is kept up to date with.
 */
@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {

    @Provides
    @Singleton
    fun providesJson(): Json = Json {
        // The service adds fields over time, and a forecast that stopped
        // parsing because a new one appeared would be a self inflicted outage.
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun providesHttpClient(json: Json): HttpClient = HttpClient(Android) {
        expectSuccess = true

        install(ContentNegotiation) { json(json) }

        install(HttpTimeout) {
            requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
            connectTimeoutMillis = CONNECT_TIMEOUT_MILLIS
            socketTimeoutMillis = SOCKET_TIMEOUT_MILLIS
        }

        // A weather service under load is worth waiting for; a request that is
        // wrong is not. Only server side failures are retried, and the delay
        // grows so a struggling service is not pushed further over.
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = MAX_RETRIES)
            exponentialDelay()
        }
    }

    private const val REQUEST_TIMEOUT_MILLIS = 15_000L
    private const val CONNECT_TIMEOUT_MILLIS = 10_000L
    private const val SOCKET_TIMEOUT_MILLIS = 15_000L
    private const val MAX_RETRIES = 2
}
