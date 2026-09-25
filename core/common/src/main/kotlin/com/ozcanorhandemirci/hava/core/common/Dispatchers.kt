package com.ozcanorhandemirci.hava.core.common

import javax.inject.Qualifier

/**
 * Names the thread pool a dependency should run on.
 *
 * Injecting the dispatcher rather than calling [kotlinx.coroutines.Dispatchers]
 * inside a class is what lets a test replace it, which is the difference
 * between a suite that runs in milliseconds and one that waits for real
 * threads.
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val kind: HavaDispatcher)

enum class HavaDispatcher {
    /** For work that waits: network calls and disk access. */
    IO,

    /** For work that computes: parsing and mapping. */
    Default,
}
