package com.nkot117.core.domain.usecase.weather

import com.nkot117.core.domain.repository.AutoWeatherSettingsRepository
import javax.inject.Inject

class ObserveAutoWeatherSettingsUseCase
@Inject
constructor(
    private val repository: AutoWeatherSettingsRepository
) {
    operator fun invoke() = repository.observeAutoWeatherSettings()
}
