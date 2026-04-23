package com.nkot117.core.domain.weather

import com.nkot117.core.domain.usecase.weather.SyncLocationPermissionOnAppStartUseCase
import com.nkot117.core.test.fake.FakeAutoWeatherSettingsRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class SyncLocationPermissionOnAppStartUseCaseTest :
    FunSpec({
        lateinit var repository: FakeAutoWeatherSettingsRepository
        lateinit var useCase: SyncLocationPermissionOnAppStartUseCase

        beforeTest {
            repository = FakeAutoWeatherSettingsRepository()
            useCase = SyncLocationPermissionOnAppStartUseCase(repository)
            repository.seed(enabled = true)
        }

        context("位置情報権限が付与されていない場合") {
            test("自動天気更新設定がfalseに更新される") {
                useCase(permissionGranted = false)

                repository.getAutoWeatherSettings() shouldBe false
            }
        }

        context("位置情報権限が付与されている場合") {
            test("自動天気更新設定が変更されない") {
                useCase(permissionGranted = true)

                repository.getAutoWeatherSettings() shouldBe true
            }
        }
    })
