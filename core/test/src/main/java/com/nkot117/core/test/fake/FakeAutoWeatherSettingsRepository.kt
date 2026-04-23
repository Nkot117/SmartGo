package com.nkot117.core.test.fake

import com.nkot117.core.domain.repository.AutoWeatherSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAutoWeatherSettingsRepository : AutoWeatherSettingsRepository {
    private val autoWeatherSettingsFlow = MutableStateFlow(false)

    fun seed(enabled: Boolean) {
        autoWeatherSettingsFlow.value = enabled
    }

    override suspend fun getAutoWeatherSettings(): Boolean = autoWeatherSettingsFlow.value

    override suspend fun saveAutoWeatherSettings(enabled: Boolean) {
        autoWeatherSettingsFlow.value = enabled
    }

    override fun observeAutoWeatherSettings(): Flow<Boolean> = autoWeatherSettingsFlow
}
