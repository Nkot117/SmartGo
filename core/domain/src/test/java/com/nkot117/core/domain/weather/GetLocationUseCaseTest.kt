package com.nkot117.core.domain.weather

import com.nkot117.core.domain.model.Location
import com.nkot117.core.domain.usecase.weather.GetLocationUseCase
import com.nkot117.core.test.fake.FakeLocationRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest

class GetLocationUseCaseTest :
    FunSpec({
        lateinit var repository: FakeLocationRepository
        lateinit var useCase: GetLocationUseCase

        beforeTest {
            repository = FakeLocationRepository()
            useCase = GetLocationUseCase(repository)
        }

        context("位置情報が取得できる場合") {
            test("Locationを返す") {
                runTest {
                    val expected = Location(latitude = 35.6895, longitude = 139.6917)
                    repository.seed(expected)

                    val result = useCase()

                    result shouldBe expected
                }
            }
        }

        context("位置情報が取得できない場合") {
            test("nullを返す") {
                runTest {
                    repository.seed(null)

                    val result = useCase()

                    result shouldBe null
                }
            }
        }
    })
