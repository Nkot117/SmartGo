package com.nkot117.feature.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkot117.core.common.toLocalDate
import com.nkot117.core.domain.model.Item
import com.nkot117.core.domain.model.ItemCategory
import com.nkot117.core.domain.model.RegisteredItemsQuery
import com.nkot117.core.domain.usecase.items.DeleteItemUseCase
import com.nkot117.core.domain.usecase.items.GetRegisteredItemsUseCase
import com.nkot117.core.domain.usecase.items.SaveItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ItemsViewModel @Inject constructor(
    private val getRegisteredItemsUseCase: GetRegisteredItemsUseCase,
    private val saveItemUseCase: SaveItemUseCase,
    private val deleteItemUseCase: DeleteItemUseCase
) : ViewModel() {
    /**
     * UiState
     */
    private val _uiState = MutableStateFlow(ItemsUiState())
    val uiState: StateFlow<ItemsUiState> = _uiState.asStateFlow()

    init {
        observeRegisteredItemList()
    }

    fun setDate(selectedDate: Long) {
        val localDate = selectedDate.toLocalDate()
        _uiState.update { it.copy(date = localDate) }
    }

    fun setCategory(category: ItemCategory) {
        _uiState.update { it.copy(category = category) }
    }

    fun setRegisterItemName(name: String) {
        _uiState.update {
            it.copy(
                form = ItemFormState(
                    name = name,
                    canSubmit = name.trim().isNotEmpty()
                )
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeRegisteredItemList() {
        viewModelScope.launch {
            uiState
                .map { state ->
                    if (state.category == ItemCategory.DATE_SPECIFIC) {
                        RegisteredItemsQuery.BySpecificDate(state.date)
                    } else {
                        RegisteredItemsQuery.ByCategory(state.category)
                    }
                }
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    getRegisteredItemsUseCase(query)
                }
                .collect { items ->
                    _uiState.update { it.copy(itemList = items.toImmutableList()) }
                }
        }
    }

    fun registerItem() {
        viewModelScope.launch {
            val state = uiState.value
            val item = Item(
                name = state.form.name,
                category = state.category
            )

            saveItemUseCase(
                item = item,
                date = if (state.category == ItemCategory.DATE_SPECIFIC) state.date else null
            )

            _uiState.update {
                it.copy(
                    form = ItemFormState()
                )
            }
        }
    }

    fun deleteItem(itemId: Long) {
        viewModelScope.launch {
            deleteItemUseCase(itemId)
        }
    }
}
