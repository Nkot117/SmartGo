package com.nkot117.core.domain.weather

import com.nkot117.core.domain.usecase.weather.ObserveAutoWeatherSettingsUseCase
import com.nkot117.core.test.fake.FakeAutoWeatherSettingsRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class ObserveAutoWeatherSettingsUseCaseTest :
    FunSpec({
        lateinit var repository: FakeAutoWeatherSettingsRepository
        lateinit var useCase: ObserveAutoWeatherSettingsUseCase

        beforeTest {
            repository = FakeAutoWeatherSettingsRepository()
            useCase = ObserveAutoWeatherSettingsUseCase(repository)
        }

        context("自動天気更新設定がtrueの場合") {
            test("FlowからtrueがEmitされる") {
                runTest {
                    repository.seed(enabled = true)

                    val result = useCase().first()

                    result shouldBe true
                }
            }
        }

        context("自動天気更新設定がfalseの場合") {
            test("FlowからfalseがEmitされる") {
                runTest {
                    repository.seed(enabled = false)

                    val result = useCase().first()

                    result shouldBe false
                }
            }
        }

        context("設定が変更された場合") {
            test("Flowが更新された値をEmitする") {
                runTest {
                    repository.seed(enabled = false)
                    val flow = useCase()

                    repository.seed(enabled = true)

                    flow.first() shouldBe true
                }
            }
        }
    })
