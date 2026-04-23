package com.nkot117.core.data.di

import com.nkot117.core.data.repository.AutoWeatherSettingRepositoryImpl
import com.nkot117.core.data.repository.DailyNoteRepositoryImpl
import com.nkot117.core.data.repository.ItemDateRepositoryImpl
import com.nkot117.core.data.repository.ItemsRepositoryImpl
import com.nkot117.core.data.repository.LocationRepositoryImpl
import com.nkot117.core.data.repository.ReminderSettingsRepositoryImpl
import com.nkot117.core.data.repository.WeatherInfoRepositoryImpl
import com.nkot117.core.domain.repository.AutoWeatherSettingsRepository
import com.nkot117.core.domain.repository.DailyNoteRepository
import com.nkot117.core.domain.repository.ItemDateRepository
import com.nkot117.core.domain.repository.ItemsRepository
import com.nkot117.core.domain.repository.LocationRepository
import com.nkot117.core.domain.repository.ReminderSettingsRepository
import com.nkot117.core.domain.repository.WeatherInfoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindItemsRepository(impl: ItemsRepositoryImpl): ItemsRepository

    @Binds
    @Singleton
    abstract fun bindSpecialDatesRepository(impl: ItemDateRepositoryImpl): ItemDateRepository

    @Binds
    @Singleton
    abstract fun bindReminderSettingRepository(
        impl: ReminderSettingsRepositoryImpl
    ): ReminderSettingsRepository

    @Binds
    @Singleton
    abstract fun bindDailyNoteRepository(impl: DailyNoteRepositoryImpl): DailyNoteRepository

    @Binds
    @Singleton
    abstract fun bindAutoWeatherSettingsRepository(
        impl: AutoWeatherSettingRepositoryImpl
    ): AutoWeatherSettingsRepository

    @Binds
    @Singleton
    abstract fun bindWeatherInfoRepository(impl: WeatherInfoRepositoryImpl): WeatherInfoRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository
}
