package com.nkot117.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkot117.core.domain.usecase.reminder.CancelReminderAlarmUseCase
import com.nkot117.core.domain.usecase.reminder.GetReminderTimeUseCase
import com.nkot117.core.domain.usecase.reminder.UpdateReminderTimeUseCase
import com.nkot117.core.domain.usecase.weather.ObserveAutoWeatherSettingsUseCase
import com.nkot117.core.domain.usecase.weather.UpdateAutoWeatherSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getReminderTimeUseCase: GetReminderTimeUseCase,
    private val updateReminderTimeUseCase: UpdateReminderTimeUseCase,
    private val cancelReminderAlarmUseCase: CancelReminderAlarmUseCase,
    private val observeAutoWeatherSettingsUseCase: ObserveAutoWeatherSettingsUseCase,
    private val updateAutoWeatherSettingsUseCase: UpdateAutoWeatherSettingsUseCase
) : ViewModel() {

    /**
     * UiState
     */
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    /**
     * UiEffect
     */
    private val _uiEffect = MutableSharedFlow<SettingsUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        fetchReminderSettings()
        observeAutoWeatherEnabled()
    }

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is ClickEvent -> clickEvent(event)
            is ReminderEvent -> reminderEvent(event)
            is PermissionEvent -> permissionEvent(event)
            is AutoWeatherSettingsEvent -> autoWeatherSettingsEvent(event)
            is DialogEvent -> dialogEvent(event)
        }
    }

    private fun clickEvent(event: ClickEvent) {
        when (event) {
            // クリックイベント
            is ClickEvent.BackClicked -> viewModelScope.launch {
                emitEffect(SettingsUiEffect.NavigateBack)
            }

            is ClickEvent.OssLicensesClicked -> viewModelScope.launch {
                emitEffect(SettingsUiEffect.OpenOssLicenses)
            }

            is ClickEvent.TimeClicked -> _uiState.update {
                it.copy(dialog = SettingsDialog.ReminderTimePicker)
            }

            is ClickEvent.SaveClicked -> viewModelScope.launch {
                // リマインダー設定が無効になっている場合は、設定を保存して戻る
                if (!uiState.value.reminder.enabled) {
                    saveReminderSettings()
                    emitEffect(SettingsUiEffect.NavigateBack)
                    return@launch
                }

                // リマインダー設定が有効になっている場合は、通知権限の確認を行う
                emitEffect(SettingsUiEffect.RequestPostNotificationsPermission)
            }
        }
    }

    private fun reminderEvent(event: ReminderEvent) {
        when (event) {
            is ReminderEvent.ReminderToggled -> setReminderEnabled(event.enabled)

            is ReminderEvent.ReminderTimePickerConfirmed -> {
                setReminderTime(event.hour, event.minute)
                _uiState.update { it.copy(dialog = null) }
            }

            is ReminderEvent.ReminderTimePickerDismissed -> _uiState.update {
                it.copy(
                    dialog = null
                )
            }
        }
    }

    private fun permissionEvent(event: PermissionEvent) {
        when (event) {
            is PermissionEvent.PostNotifications -> {
                if (event.granted) {
                    viewModelScope.launch {
                        emitEffect(
                            SettingsUiEffect.RequestExactAlarmPermission
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(dialog = SettingsDialog.NotificationRequiredDialog)
                    }
                }
            }

            is PermissionEvent.Location -> {
                viewModelScope.launch {
                    if (event.granted) {
                        saveAutoWeatherSettings(enabled = true)
                    } else {
                        _uiState.update {
                            it.copy(
                                dialog = SettingsDialog.LocationRequiredDialog
                            )
                        }
                    }
                }
            }

            is PermissionEvent.ExactAlarm -> {
                if (event.granted) {
                    viewModelScope.launch {
                        saveReminderSettings()
                        emitEffect(SettingsUiEffect.NavigateBack)
                    }
                } else {
                    _uiState.update {
                        it.copy(dialog = SettingsDialog.ExactAlarmRequiredDialog)
                    }
                }
            }
        }
    }

    private fun autoWeatherSettingsEvent(event: AutoWeatherSettingsEvent) {
        when (event) {
            is AutoWeatherSettingsEvent.AutoWeatherToggled -> {
                if (event.enabled) {
                    viewModelScope.launch {
                        emitEffect(
                            SettingsUiEffect.RequestLocationPermission
                        )
                    }
                } else {
                    viewModelScope.launch {
                        saveAutoWeatherSettings(enabled = false)
                    }
                }
            }
        }
    }

    private fun dialogEvent(event: DialogEvent) {
        when (event) {
            is DialogEvent.NotificationRequiredDialogDismissed -> _uiState.update {
                it.copy(
                    dialog = null
                )
            }

            is DialogEvent.NotificationRequiredDialogConfirmed
            -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(dialog = null) }
                    emitEffect(SettingsUiEffect.OpenNotificationSettings)
                }
            }

            DialogEvent.ExactAlarmRequiredDialogConfirmed ->
                viewModelScope.launch {
                    _uiState.update { it.copy(dialog = null) }
                    emitEffect(SettingsUiEffect.OpenExactAlarmSettings)
                }

            DialogEvent.ExactAlarmRequiredDialogDismissed -> {
                _uiState.update {
                    it.copy(
                        dialog = null
                    )
                }
            }

            DialogEvent.LocationPermissionDialogConfirmed -> {
                viewModelScope.launch {
                    _uiState.update {
                        it.copy(dialog = null)
                    }
                    emitEffect(
                        SettingsUiEffect.OpenLocationSettings
                    )
                }
            }

            DialogEvent.LocationPermissionDialogDismissed -> {
                _uiState.update {
                    it.copy(dialog = null)
                }
            }
        }
    }

    private suspend fun emitEffect(effect: SettingsUiEffect) {
        _uiEffect.emit(effect)
    }

    private fun fetchReminderSettings() {
        viewModelScope.launch {
            val reminder = getReminderTimeUseCase()
            _uiState.update {
                it.copy(
                    reminder = reminder
                )
            }
        }
    }

    private fun observeAutoWeatherEnabled() {
        viewModelScope.launch {
            observeAutoWeatherSettingsUseCase().collect { enabled ->
                _uiState.update {
                    it.copy(
                        autoWeatherSettings = enabled
                    )
                }
            }
        }
    }

    private fun setReminderEnabled(enabled: Boolean) {
        _uiState.update {
            it.copy(
                reminder = it.reminder.copy(
                    enabled = enabled
                )
            )
        }
    }

    private fun setReminderTime(hour: Int, minute: Int) {
        _uiState.update {
            it.copy(
                reminder = it.reminder.copy(
                    hour = hour,
                    minute = minute
                )
            )
        }
    }

    private suspend fun saveReminderSettings() {
        val state = uiState.value

        updateReminderTimeUseCase(
            hour = state.reminder.hour,
            minute = state.reminder.minute,
            enabled = state.reminder.enabled
        )

        if (!state.reminder.enabled) {
            cancelReminderAlarmUseCase()
        }
    }

    private suspend fun saveAutoWeatherSettings(enabled: Boolean) {
        updateAutoWeatherSettingsUseCase(enabled)
    }
}
