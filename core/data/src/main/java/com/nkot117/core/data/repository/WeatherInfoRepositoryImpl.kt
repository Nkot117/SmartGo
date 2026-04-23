package com.nkot117.core.data.repository

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.nkot117.core.data.api.openmeteo.OpenMeteoApiService
import com.nkot117.core.data.api.openmeteo.dto.OpenMeteoRequest
import com.nkot117.core.data.di.IODispatcher
import com.nkot117.core.domain.model.AppError
import com.nkot117.core.domain.model.DailyWeatherInfo
import com.nkot117.core.domain.repository.WeatherInfoRepository
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.UnknownHostException
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class WeatherInfoRepositoryImpl @Inject constructor(
    private val apiService: OpenMeteoApiService,
    @param:IODispatcher private val io: CoroutineDispatcher
) : WeatherInfoRepository {
    override suspend fun getCurrentLocationDailyWeatherInfo(
        latitude: Double,
        longitude: Double
    ): Result<DailyWeatherInfo, AppError> = withContext(io) {
        val requestParams = OpenMeteoRequest(
            latitude = latitude,
            longitude = longitude,
            forecastDays = 1,
            daily = listOf("weather_code"),
            timezone = "Asia/Tokyo"
        )

        runSuspendCatching {
            apiService.getDailyWeatherInfo(requestParams)
        }.map {
            DailyWeatherInfo(
                weatherCode = it.daily.weatherCode.firstOrNull() ?: 0
            )
        }.mapError { throwable ->
            when (throwable) {
                is HttpRequestTimeoutException,
                is ConnectTimeoutException,
                is SocketTimeoutException
                -> AppError.Timeout

                is UnknownHostException,
                is ConnectException,
                is NoRouteToHostException
                -> AppError.NetworkUnavailable

                is ServerResponseException
                -> AppError.ServerError

                else -> AppError.Unknown
            }
        }
    }
}
