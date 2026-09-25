package com.ozcanorhandemirci.hava.core.data.di

import com.ozcanorhandemirci.hava.core.data.CityRepository
import com.ozcanorhandemirci.hava.core.data.WeatherRepository
import com.ozcanorhandemirci.hava.core.data.internal.OfflineFirstCityRepository
import com.ozcanorhandemirci.hava.core.data.internal.OfflineFirstWeatherRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface DataModule {

    @Binds
    @Singleton
    fun bindsCityRepository(repository: OfflineFirstCityRepository): CityRepository

    @Binds
    @Singleton
    fun bindsWeatherRepository(repository: OfflineFirstWeatherRepository): WeatherRepository
}
