package com.nkot117.core.domain.usecase.items

import com.nkot117.core.domain.model.DayType
import com.nkot117.core.domain.model.Item
import com.nkot117.core.domain.model.ItemCategory
import com.nkot117.core.domain.model.WeatherType
import com.nkot117.core.domain.repository.ItemDateRepository
import com.nkot117.core.domain.repository.ItemsRepository
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetItemsToBringUseCase
@Inject
constructor(
    private val itemsRepository: ItemsRepository,
    private val itemDateRepository: ItemDateRepository
) {
    operator fun invoke(
        dayType: DayType,
        weatherType: WeatherType,
        date: LocalDate
    ): Flow<List<Item>> = combine(
        itemsRepository.getAllItems(),
        itemDateRepository.getItemIdsOnDate(date)
    ) { all, specialItemIds ->
        all.filter { item ->
            when (item.category) {
                ItemCategory.ALWAYS -> true
                ItemCategory.WORKDAY -> dayType == DayType.WORKDAY
                ItemCategory.HOLIDAY -> dayType == DayType.HOLIDAY
                ItemCategory.RAINY -> weatherType == WeatherType.RAINY
                ItemCategory.SUNNY -> weatherType == WeatherType.SUNNY
                ItemCategory.DATE_SPECIFIC -> item.id in specialItemIds
            }
        }
    }
}
