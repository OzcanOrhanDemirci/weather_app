package com.ozcanorhandemirci.hava.core.common

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

/**
 * The clock, injected rather than read from a static.
 *
 * Everything in this application is a function of the time: which hours the
 * forecast shows, whether the cache is stale, where the sun is. A test that
 * cannot choose the moment can only assert that something happened, not that
 * the right thing happened at the right time.
 */
@Module
@InstallIn(SingletonComponent::class)
internal object TimeModule {

    @Provides
    @Singleton
    fun providesClock(): Clock = Clock.systemUTC()
}
