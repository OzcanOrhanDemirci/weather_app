package com.ozcanorhandemirci.hava.core.model

/**
 * The result of an operation that can fail in ways the interface must react to.
 *
 * A thrown exception would carry the same information, but it would carry it
 * out of band, and every caller would be free to forget it. Returning the
 * failure makes handling it part of the signature.
 */
sealed interface Outcome<out T> {

    data class Success<T>(val value: T) : Outcome<T>

    data class Failure(val reason: WeatherError) : Outcome<Nothing>

    val valueOrNull: T?
        get() = (this as? Success)?.value

    fun <R> map(transform: (T) -> R): Outcome<R> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }
}

/**
 * Why a request for weather did not produce weather.
 *
 * Each case exists because the interface says something different about it. A
 * device with no connection is offered what was last stored; a server that is
 * having trouble is offered a retry; a response that cannot be read is a defect
 * and says so rather than pretending to be a network problem.
 */
sealed interface WeatherError {

    /** No usable network connection. */
    data object Offline : WeatherError

    /** A connection that was made but did not answer in time. */
    data object Timeout : WeatherError

    /** The service answered, and the answer was not success. */
    data class Service(val statusCode: Int) : WeatherError

    /** The service answered with something this application cannot read. */
    data object Unreadable : WeatherError

    /** Asked for a place the service does not know. */
    data object UnknownPlace : WeatherError

    data class Unexpected(val cause: Throwable) : WeatherError
}
