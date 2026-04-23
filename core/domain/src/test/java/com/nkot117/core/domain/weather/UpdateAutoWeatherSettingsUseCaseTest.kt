package com.nkot117.core.domain.weather

import com.nkot117.core.domain.usecase.weather.UpdateAutoWeatherSettingsUseCase
import com.nkot117.core.test.fake.FakeAutoWeatherSettingsRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest

class UpdateAutoWeatherSettingsUseCaseTest :
    FunSpec({
        lateinit var repository: FakeAutoWeatherSettingsRepository
        lateinit var useCase: UpdateAutoWeatherSettingsUseCase

        beforeTest {
            repository = FakeAutoWeatherSettingsRepository()
            useCase = UpdateAutoWeatherSettingsUseCase(repository)
        }

        context("trueを渡した場合") {
            test("リポジトリの設定がtrueに更新される") {
                runTest {
                    useCase(enabled = true)

                    repository.getAutoWeatherSettings() shouldBe true
                }
            }
        }

        context("falseを渡した場合") {
            test("リポジトリの設定がfalseに更新される") {
                runTest {
                    repository.seed(enabled = true)

                    useCase(enabled = false)

                    repository.getAutoWeatherSettings() shouldBe false
                }
            }
        }
    })
