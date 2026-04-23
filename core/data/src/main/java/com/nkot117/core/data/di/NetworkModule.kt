package com.nkot117.core.data.di

import com.nkot117.core.data.api.ApiClient
import com.nkot117.core.data.api.KtorHttpClientFactory
import com.nkot117.core.data.api.openmeteo.OpenMeteoApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideKtorHttpClient(): HttpClient = KtorHttpClientFactory().create()

    @Provides
    @Singleton
    fun provideOpenMeteoApiService(httpClient: HttpClient): OpenMeteoApiService {
        val apiClient = ApiClient(
            httpClient = httpClient
        )
        return OpenMeteoApiService(apiClient)
    }
}
