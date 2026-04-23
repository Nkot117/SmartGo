package com.nkot117.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import com.nkot117.core.common.toLocalDate
import com.nkot117.core.domain.model.DayType
import com.nkot117.core.domain.model.WeatherType
import com.nkot117.core.domain.usecase.dailynote.GetDailyNoteUseCase
import com.nkot117.core.domain.usecase.dailynote.SaveDailyNoteUseCase
import com.nkot117.core.domain.usecase.items.GetItemsToBringUseCase
import com.nkot117.core.domain.usecase.weather.GetAutoWeatherSettingsUseCase
import com.nkot117.core.domain.usecase.weather.GetCurrentLocationDailyWeatherTypeUseCase
import com.nkot117.core.domain.usecase.weather.toWeatherType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getItemsToBringUseCase: GetItemsToBringUseCase,
    private val getDailyNoteUseCase: GetDailyNoteUseCase,
    private val saveDailyNoteUseCase: SaveDailyNoteUseCase,
    private val getAutoWeatherSettingsUseCase: GetAutoWeatherSettingsUseCase,
    private val getCurrentLocationDailyWeatherTypeUseCase: GetCurrentLocationDailyWeatherTypeUseCase
) : ViewModel() {
    /**
     * UiState
     */
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /**
     * UiEffect
     */
    private val _uiEffect = MutableSharedFlow<HomeUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        observeDailyNote()
        observeChecklist()
        fetchCurrentWeatherIfNeeded()
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is ClickEvent -> clickEvent(event)
            is DialogEvent -> dialogEvent(event)
        }
    }

    private fun clickEvent(event: ClickEvent) {
        when (event) {
            ClickEvent.DailyNoteClicked -> _uiState.update {
                it.copy(dialog = HomeDialog.DailyNoteEditDialog)
            }

            is ClickEvent.DailyTypeToggled -> setDayType(event.dayType)

            is ClickEvent.WeatherTypeToggled -> {
                setWeatherType(event.weatherType)
            }

            ClickEvent.ChecklistClicked -> {
                viewModelScope.launch {
                    _uiEffect.emit(HomeUiEffect.TransitionToChecklistScreen)
                }
            }
        }
    }

    private fun dialogEvent(event: DialogEvent) {
        when (event) {
            is DialogEvent.CalendarDialogConfirmed -> {
                val localDate = event.selectedDate.toLocalDate()
                _uiState.update {
                    it.copy(dialog = null, date = localDate)
                }
            }

            is DialogEvent.DailyNoteEditDialogConfirmed -> {
                _uiState.update {
                    it.copy(dialog = null, dailyNote = event.note)
                }

                saveDailyNote(event.note)
            }

            DialogEvent.CalendarDialogDismissed,
            DialogEvent.DailyNoteEditDialogDismissed,
            DialogEvent.AutoWeatherSettingsErrorDialogDismissed
            -> _uiState.update {
                it.copy(dialog = null)
            }
        }
    }

    private fun observeChecklist() {
        viewModelScope.launch {
            uiState
                .map { Triple(it.dayType, it.weatherType, it.date) }
                .distinctUntilChanged()
                .collectLatest {
                    val (dayType, weatherType, date) = it
                    val items = getItemsToBringUseCase(
                        dayType = dayType,
                        weatherType = weatherType,
                        date = date
                    )
                    _uiState.update { state -> state.copy(preview = items.toImmutableList()) }
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeDailyNote() {
        viewModelScope.launch {
            uiState
                .map { it.date }
                .distinctUntilChanged()
                .flatMapLatest { date -> getDailyNoteUseCase(date) }
                .collect { note ->
                    _uiState.update { it.copy(dailyNote = note?.text.orEmpty()) }
                }
        }
    }

    private fun setDayType(dayType: DayType) {
        _uiState.update { it.copy(dayType = dayType) }
    }

    private fun setWeatherType(weatherType: WeatherType) {
        _uiState.update { it.copy(weatherType = weatherType) }
    }

    private fun saveDailyNote(note: String) {
        viewModelScope.launch {
            val date = _uiState.value.date
            saveDailyNoteUseCase(date, note)
        }
    }

    private fun fetchCurrentWeatherIfNeeded() {
        viewModelScope.launch {
            val autoWeatherEnabled = getAutoWeatherSettingsUseCase()
            if (!autoWeatherEnabled) return@launch

            _uiState.update { it.copy(isLoadingWeather = true) }
            getCurrentLocationDailyWeatherTypeUseCase().onOk { dailyWeatherInfo ->
                _uiState.update {
                    it.copy(
                        isLoadingWeather = false,
                        weatherType = dailyWeatherInfo.weatherCode.toWeatherType()
                    )
                }
            }.onErr {
                _uiState.update {
                    it.copy(
                        isLoadingWeather = false,
                        dialog = HomeDialog.AutoWeatherSettingsErrorDialog
                    )
                }
            }
        }
    }
}
