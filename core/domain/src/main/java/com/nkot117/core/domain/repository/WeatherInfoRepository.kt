package com.nkot117.core.domain.repository

import com.github.michaelbull.result.Result
import com.nkot117.core.domain.model.AppError
import com.nkot117.core.domain.model.DailyWeatherInfo

interface WeatherInfoRepository {
    suspend fun getCurrentLocationDailyWeatherInfo(
        latitude: Double,
        longitude: Double
    ): Result<DailyWeatherInfo, AppError>
}
