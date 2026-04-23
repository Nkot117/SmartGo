package com.nkot117.core.domain.weather

import com.nkot117.core.domain.usecase.weather.GetAutoWeatherSettingsUseCase
import com.nkot117.core.test.fake.FakeAutoWeatherSettingsRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest

class GetAutoWeatherSettingsUseCaseTest :
    FunSpec({
        lateinit var repository: FakeAutoWeatherSettingsRepository
        lateinit var useCase: GetAutoWeatherSettingsUseCase

        beforeTest {
            repository = FakeAutoWeatherSettingsRepository()
            useCase = GetAutoWeatherSettingsUseCase(repository)
        }

        context("自動天気更新設定がtrueの場合") {
            test("trueを返す") {
                runTest {
                    repository.seed(enabled = true)

                    val result = useCase()

                    result shouldBe true
                }
            }
        }

        context("自動天気更新設定がfalseの場合") {
            test("falseを返す") {
                runTest {
                    repository.seed(enabled = false)

                    val result = useCase()

                    result shouldBe false
                }
            }
        }
    })
