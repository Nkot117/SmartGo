package com.nkot117.core.domain.usecase.weather

import com.nkot117.core.domain.repository.AutoWeatherSettingsRepository
import javax.inject.Inject

class UpdateAutoWeatherSettingsUseCase
@Inject
constructor(
    val repository: AutoWeatherSettingsRepository
) {
    /**
     * 自動天気更新の設定を更新するユースケース
     *
     * @param enabled 自動天気更新が有効かどうか
     */
    suspend operator fun invoke(enabled: Boolean) = repository.saveAutoWeatherSettings(enabled)
}
