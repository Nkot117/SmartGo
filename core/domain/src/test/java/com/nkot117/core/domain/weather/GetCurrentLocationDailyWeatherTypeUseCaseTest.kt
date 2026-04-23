package com.nkot117.core.domain.weather

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.nkot117.core.domain.model.AppError
import com.nkot117.core.domain.model.DailyWeatherInfo
import com.nkot117.core.domain.model.Location
import com.nkot117.core.domain.model.WeatherType
import com.nkot117.core.domain.usecase.weather.GetCurrentLocationDailyWeatherTypeUseCase
import com.nkot117.core.domain.usecase.weather.GetLocationUseCase
import com.nkot117.core.domain.usecase.weather.toWeatherType
import com.nkot117.core.test.fake.FakeLocationRepository
import com.nkot117.core.test.fake.FakeWeatherInfoRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest

class GetCurrentLocationDailyWeatherTypeUseCaseTest :
    FunSpec({
        lateinit var locationRepository: FakeLocationRepository
        lateinit var weatherInfoRepository: FakeWeatherInfoRepository
        lateinit var useCase: GetCurrentLocationDailyWeatherTypeUseCase

        beforeTest {
            locationRepository = FakeLocationRepository()
            weatherInfoRepository = FakeWeatherInfoRepository()
            useCase = GetCurrentLocationDailyWeatherTypeUseCase(
                weatherInfoRepository = weatherInfoRepository,
                getLocationUseCase = GetLocationUseCase(locationRepository)
            )
        }

        context("位置情報が取得できる場合") {
            test("取得した位置情報を使って天気情報を取得する") {
                runTest {
                    locationRepository.seed(Location(latitude = 35.6895, longitude = 139.6917))
                    weatherInfoRepository.seed(Ok(DailyWeatherInfo(weatherCode = 0)))

                    useCase()

                    weatherInfoRepository.lastRequestedLatitude shouldBe 35.6895
                    weatherInfoRepository.lastRequestedLongitude shouldBe 139.6917
                }
            }

            test("天気情報取得が成功した場合、Ok(DailyWeatherInfo)を返す") {
                runTest {
                    locationRepository.seed(Location(latitude = 35.6895, longitude = 139.6917))
                    val expected = DailyWeatherInfo(weatherCode = 1)
                    weatherInfoRepository.seed(Ok(expected))

                    val result = useCase()

                    result shouldBe Ok(expected)
                }
            }

            test("天気情報取得が失敗した場合、Err(AppError)を返す") {
                runTest {
                    locationRepository.seed(Location(latitude = 35.6895, longitude = 139.6917))
                    weatherInfoRepository.seed(Err(AppError.NetworkUnavailable))

                    val result = useCase()

                    result shouldBe Err(AppError.NetworkUnavailable)
                }
            }
        }

        context("位置情報が取得できない場合") {
            test("緯度経度(0.0, 0.0)を使って天気情報を取得する") {
                runTest {
                    locationRepository.seed(null)
                    weatherInfoRepository.seed(Ok(DailyWeatherInfo(weatherCode = 0)))

                    useCase()

                    weatherInfoRepository.lastRequestedLatitude shouldBe 0.0
                    weatherInfoRepository.lastRequestedLongitude shouldBe 0.0
                }
            }
        }
    })

class ToWeatherTypeTest :
    FunSpec({
        context("天気コードが0, 1, 2の場合") {
            listOf(0, 1, 2).forEach { code ->
                test("天気コード $code はSUNNYを返す") {
                    code.toWeatherType() shouldBe WeatherType.SUNNY
                }
            }
        }

        context("天気コードが3以上の場合") {
            listOf(3, 10, 99).forEach { code ->
                test("天気コード $code はRAINYを返す") {
                    code.toWeatherType() shouldBe WeatherType.RAINY
                }
            }
        }
    })
