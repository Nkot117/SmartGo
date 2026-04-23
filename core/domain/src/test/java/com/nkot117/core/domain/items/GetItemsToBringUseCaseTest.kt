package com.nkot117.core.domain.items

import com.nkot117.core.domain.model.DayType
import com.nkot117.core.domain.model.Item
import com.nkot117.core.domain.model.ItemCategory
import com.nkot117.core.domain.model.WeatherType
import com.nkot117.core.domain.usecase.items.GetItemsToBringUseCase
import com.nkot117.core.test.fake.FakeItemDateRepository
import com.nkot117.core.test.fake.FakeItemsRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import kotlinx.coroutines.flow.first

class GetItemsToBringUseCaseTest :
    FunSpec({
        lateinit var itemsRepository: FakeItemsRepository
        lateinit var specialItemDateRepository: FakeItemDateRepository
        lateinit var useCase: GetItemsToBringUseCase

        beforeTest {
            itemsRepository = FakeItemsRepository()
            specialItemDateRepository = FakeItemDateRepository()
            useCase =
                GetItemsToBringUseCase(
                    itemsRepository = itemsRepository,
                    itemDateRepository = specialItemDateRepository
                )

            itemsRepository.seedItems(
                Item(id = 1, name = "財布", category = ItemCategory.ALWAYS),
                Item(id = 2, name = "スマホ", category = ItemCategory.ALWAYS),
                Item(id = 3, name = "傘", category = ItemCategory.RAINY),
                Item(id = 4, name = "定期券", category = ItemCategory.WORKDAY),
                Item(id = 5, name = "サングラス", category = ItemCategory.SUNNY),
                Item(id = 6, name = "友達のプレゼント", category = ItemCategory.DATE_SPECIFIC),
                Item(id = 7, name = "本", category = ItemCategory.HOLIDAY),
                Item(id = 8, name = "レンタルCD", category = ItemCategory.DATE_SPECIFIC)
            )
            specialItemDateRepository.seedItems(
                Item(id = 6, name = "友達のプレゼント", category = ItemCategory.DATE_SPECIFIC),
                Item(id = 8, name = "レンタルCD", category = ItemCategory.DATE_SPECIFIC)
            )

            specialItemDateRepository.seedDateItemIds(
                LocalDate.of(2026, 2, 1) to listOf(6),
                LocalDate.of(2026, 3, 1) to listOf(8)
            )
        }

        test("「いつも」カテゴリのアイテムは常に返却されること") {
            // Arrange
            // NOP

            // Act
            val result =
                useCase(
                    dayType = DayType.WORKDAY,
                    weatherType = WeatherType.SUNNY,
                    date = LocalDate.of(2026, 2, 1)
                ).first()

            // Assert
            val alwaysItems = result.filter { it.category == ItemCategory.ALWAYS }

            alwaysItems shouldContainExactlyInAnyOrder
                listOf(
                    Item(id = 1, name = "財布", category = ItemCategory.ALWAYS),
                    Item(id = 2, name = "スマホ", category = ItemCategory.ALWAYS)
                )
        }

        context(
            "dayType：「勤務日」カテゴリを指定した場合"
        ) {
            lateinit var result: List<Item>

            beforeTest {
                result =
                    useCase(
                        dayType = DayType.WORKDAY,
                        weatherType = WeatherType.SUNNY,
                        date = LocalDate.of(2026, 2, 1)
                    ).first()
            }

            test("「勤務日」カテゴリのアイテムが返却されること") {
                // Assert
                val workdayItems = result.filter { it.category == ItemCategory.WORKDAY }
                workdayItems[0] shouldBe Item(id = 4, name = "定期券", category = ItemCategory.WORKDAY)
            }

            test("「休日」カテゴリのアイテムが返却されないこと") {
                // Assert
                val holidayItems = result.filter { it.category == ItemCategory.HOLIDAY }
                holidayItems.shouldBeEmpty()
            }
        }

        context("dayType：「休日」カテゴリを指定した場合") {
            lateinit var result: List<Item>
            beforeTest {
                result =
                    useCase(
                        dayType = DayType.HOLIDAY,
                        weatherType = WeatherType.SUNNY,
                        date = LocalDate.of(2026, 2, 1)
                    ).first()
            }

            test("「勤務日」カテゴリのアイテムが返却されないこと") {
                // Assert
                val workdayItems = result.filter { it.category == ItemCategory.WORKDAY }
                workdayItems.shouldBeEmpty()
            }

            test("「休日」カテゴリのアイテムが返却されること") {
                // Assert
                val holidayItems = result.filter { it.category == ItemCategory.HOLIDAY }
                holidayItems[0] shouldBe Item(id = 7, name = "本", category = ItemCategory.HOLIDAY)
            }
        }

        context("weatherType：「雨」を指定した場合") {
            lateinit var result: List<Item>
            beforeTest {
                result =
                    useCase(
                        dayType = DayType.WORKDAY,
                        weatherType = WeatherType.RAINY,
                        date = LocalDate.of(2026, 2, 1)
                    ).first()
            }

            test("「雨」カテゴリのアイテムが返却されること") {
                // Assert
                val rainyItems = result.filter { it.category == ItemCategory.RAINY }
                rainyItems[0] shouldBe Item(id = 3, name = "傘", category = ItemCategory.RAINY)
            }

            test("「晴れ」カテゴリのアイテムが返却されないこと") {
                // Assert
                val sunnyItems = result.filter { it.category == ItemCategory.SUNNY }
                sunnyItems.shouldBeEmpty()
            }
        }

        context("weatherType：「晴れ」を指定した場合") {
            lateinit var result: List<Item>
            beforeTest {
                result =
                    useCase(
                        dayType = DayType.WORKDAY,
                        weatherType = WeatherType.SUNNY,
                        date = LocalDate.of(2026, 2, 1)
                    ).first()
            }
            test("「雨」カテゴリのアイテムが返却されないこと") {
                // Assert
                val rainyItems = result.filter { it.category == ItemCategory.RAINY }
                rainyItems.shouldBeEmpty()
            }

            test("「晴れ」カテゴリのアイテムが返却されること") {
                // Assert
                val sunnyItems = result.filter { it.category == ItemCategory.SUNNY }
                sunnyItems[0] shouldBe Item(id = 5, name = "サングラス", category = ItemCategory.SUNNY)
            }
        }

        context("date：2026-02-01を指定した場合") {
            lateinit var result: List<Item>

            beforeTest {
                result =
                    useCase(
                        dayType = DayType.WORKDAY,
                        weatherType = WeatherType.SUNNY,
                        date = LocalDate.of(2026, 2, 1)
                    ).first()
            }

            test("DATE_SPECIFICはid=6のみが含まれること") {
                val dateSpecificIds =
                    result
                        .filter { it.category == ItemCategory.DATE_SPECIFIC }
                        .map { it.id }

                dateSpecificIds shouldBe listOf(6)
            }
        }
    })
