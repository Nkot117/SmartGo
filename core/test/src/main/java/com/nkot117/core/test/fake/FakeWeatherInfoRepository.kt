package com.nkot117.core.test.fake

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.nkot117.core.domain.model.AppError
import com.nkot117.core.domain.model.DailyWeatherInfo
import com.nkot117.core.domain.repository.WeatherInfoRepository

class FakeWeatherInfoRepository : WeatherInfoRepository {
    private var result: Result<DailyWeatherInfo, AppError> = Ok(DailyWeatherInfo(weatherCode = 0))
    var lastRequestedLatitude: Double? = null
    var lastRequestedLongitude: Double? = null

    fun seed(result: Result<DailyWeatherInfo, AppError>) {
        this.result = result
    }

    override suspend fun getCurrentLocationDailyWeatherInfo(
        latitude: Double,
        longitude: Double
    ): Result<DailyWeatherInfo, AppError> {
        lastRequestedLatitude = latitude
        lastRequestedLongitude = longitude
        return result
    }
}
