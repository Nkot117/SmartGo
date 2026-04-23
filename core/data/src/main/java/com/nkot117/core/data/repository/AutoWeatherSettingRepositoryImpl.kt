package com.nkot117.core.data.repository

import com.nkot117.core.data.datastore.AutoWeatherSettingsDataSource
import com.nkot117.core.data.di.IODispatcher
import com.nkot117.core.domain.repository.AutoWeatherSettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class AutoWeatherSettingRepositoryImpl
@Inject
constructor(
    private val dataSource: AutoWeatherSettingsDataSource,
    @param:IODispatcher private val io: CoroutineDispatcher
) : AutoWeatherSettingsRepository {
    override suspend fun getAutoWeatherSettings(): Boolean = withContext(io) {
        dataSource.getAutoWeatherSetting()
    }

    override suspend fun saveAutoWeatherSettings(enabled: Boolean) {
        withContext(io) {
            dataSource.saveAutoWeatherSetting(enabled)
        }
    }

    override fun observeAutoWeatherSettings(): Flow<Boolean> =
        dataSource.observeAutoWeatherSetting().flowOn(io)
}
