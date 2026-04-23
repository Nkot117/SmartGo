package com.nkot117.core.domain.usecase.weather

import com.nkot117.core.domain.repository.AutoWeatherSettingsRepository
import javax.inject.Inject

class GetAutoWeatherSettingsUseCase
@Inject
constructor(
    private val repository: AutoWeatherSettingsRepository
) {
    /**
     * 自動天気設定を取得するユースケース
     */
    suspend operator fun invoke() = repository.getAutoWeatherSettings()
}
