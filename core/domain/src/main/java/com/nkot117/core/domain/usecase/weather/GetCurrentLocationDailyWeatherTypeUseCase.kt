package com.nkot117.core.domain.usecase.weather

import com.github.michaelbull.result.Result
import com.nkot117.core.domain.model.AppError
import com.nkot117.core.domain.model.DailyWeatherInfo
import com.nkot117.core.domain.model.WeatherType
import com.nkot117.core.domain.repository.WeatherInfoRepository
import javax.inject.Inject

class GetCurrentLocationDailyWeatherTypeUseCase @Inject constructor(
    private val weatherInfoRepository: WeatherInfoRepository,
    private val getLocationUseCase: GetLocationUseCase
) {
    /**
     * 現在地のその日の天気種別を取得するユースケース
     *
     * Repositoryから取得した日次天気情報の取得結果を返却する。
     *
     */
    suspend operator fun invoke(): Result<DailyWeatherInfo, AppError> {
        val location = getLocationUseCase()
        return weatherInfoRepository.getCurrentLocationDailyWeatherInfo(
            latitude = location?.latitude ?: 0.0,
            longitude = location?.longitude ?: 0.0
        )
    }
}

fun Int.toWeatherType(): WeatherType = when (this) {
    0, 1, 2 -> WeatherType.SUNNY
    else -> WeatherType.RAINY
}
