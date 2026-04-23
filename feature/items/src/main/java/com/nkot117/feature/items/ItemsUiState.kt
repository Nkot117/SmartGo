package com.nkot117.feature.items

import com.nkot117.core.domain.model.Item
import com.nkot117.core.domain.model.ItemCategory
import java.time.LocalDate
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ItemFormState(val name: String = "", val canSubmit: Boolean = false)

data class ItemsUiState(
    val date: LocalDate = LocalDate.now(),
    val category: ItemCategory = ItemCategory.ALWAYS,
    val itemList: ImmutableList<Item> = persistentListOf(),
    val form: ItemFormState = ItemFormState()
)
