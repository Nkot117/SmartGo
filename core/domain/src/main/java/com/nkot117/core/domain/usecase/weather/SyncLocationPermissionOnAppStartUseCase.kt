package com.nkot117.core.domain.usecase.weather

import com.nkot117.core.domain.repository.AutoWeatherSettingsRepository
import javax.inject.Inject

class SyncLocationPermissionOnAppStartUseCase
@Inject
constructor(
    private val autoWeatherSettingsRepository: AutoWeatherSettingsRepository
) {
    /**
     * アプリ起動時に自動天気更新の設定を同期するユースケース
     *
     * 位置情報権限が付与されていない場合は、自動天気更新の設定を無効に設定する
     *
     * @param permissionGranted 位置情報権限が付与されているかどうか
     */
    suspend operator fun invoke(permissionGranted: Boolean) {
        // 権限が付与されている場合は何もしない
        if (permissionGranted) return

        // 位置情報権限が付与されていない場合は、自動天気更新の設定を無効にする
        autoWeatherSettingsRepository.saveAutoWeatherSettings(enabled = false)
    }
}
