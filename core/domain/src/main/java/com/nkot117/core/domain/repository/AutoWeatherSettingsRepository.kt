package com.nkot117.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface AutoWeatherSettingsRepository {
    suspend fun getAutoWeatherSettings(): Boolean
    suspend fun saveAutoWeatherSettings(enabled: Boolean)
    fun observeAutoWeatherSettings(): Flow<Boolean>
}
