package com.nkot117.core.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.autoWeatherDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "auto_weather_settings"
)

class AutoWeatherSettingsDataSource
@Inject
constructor(
    @param:ApplicationContext private val context: Context
) {

    private object Keys {
        val ENABLED = booleanPreferencesKey("enabled")
    }

    fun observeAutoWeatherSetting(): Flow<Boolean> = context.autoWeatherDataStore.data.map { pref ->
        pref[Keys.ENABLED] ?: DEFAULT_ENABLED
    }

    suspend fun getAutoWeatherSetting(): Boolean = observeAutoWeatherSetting().first()

    suspend fun saveAutoWeatherSetting(enabled: Boolean) {
        context.autoWeatherDataStore.edit { pref ->
            pref[Keys.ENABLED] = enabled
        }
    }

    private companion object {
        const val DEFAULT_ENABLED = false
    }
}
