package com.ozcanorhandemirci.hava.core.network

/**
 * A forecast response exactly as the service sent it.
 *
 * The document is carried rather than a parsed object because it is also what
 * gets stored. Keeping the response verbatim means there is one path from the
 * wire format to the domain, used both when the answer arrives and when it is
 * read back from the cache days later, so a cached forecast can never be
 * mapped differently from a fresh one.
 */
@JvmInline
value class ForecastDocument(val json: String)
