package com.nkot117.feature.settings

import com.nkot117.core.domain.model.Reminder

/**
 * Settings画面のUI状態を表すデータクラス
 *
 */
data class SettingsUiState(
    val reminder: Reminder = Reminder(9, 0, false),
    val dialog: SettingsDialog? = null
)

/**
 * Settings画面で表示されるダイアログの種類を表すシールクラス
 */
sealed interface SettingsDialog {
    data object ReminderTimePicker : SettingsDialog
    data object NotificationRequiredDialog : SettingsDialog
    data object ExactAlarmRequiredDialog : SettingsDialog
}

/**
 * Settings画面のUIイベントを表すシールクラス
 */
sealed interface SettingsUiEvent

/**
 * クリックイベント
 */
sealed interface ClickEvent : SettingsUiEvent {
    data object BackClicked : ClickEvent
    data object OssLicensesClicked : ClickEvent
    data object TimeClicked : ClickEvent
    data object SaveClicked : ClickEvent
}

/**
 * リマインダー設定イベント
 */
sealed interface ReminderEvent : SettingsUiEvent {
    data class ReminderToggled(val enabled: Boolean) : ReminderEvent
    data class ReminderTimePickerConfirmed(val hour: Int, val minute: Int) : ReminderEvent
    data object ReminderTimePickerDismissed : ReminderEvent
}

/**
 * ダイアログイベント
 */
sealed interface DialogEvent : SettingsUiEvent {
    data object NotificationRequiredDialogConfirmed : DialogEvent
    data object NotificationRequiredDialogDismissed : DialogEvent
    data object ExactAlarmRequiredDialogConfirmed : DialogEvent
    data object ExactAlarmRequiredDialogDismissed : DialogEvent
}

/**
 * パーミッションイベント
 */
sealed interface PermissionEvent : SettingsUiEvent {
    data class PostNotifications(val granted: Boolean) : PermissionEvent
    data class ExactAlarm(val granted: Boolean) : PermissionEvent
}

/**
 * Settings画面で実行する副作用を表すシールクラス
 */
sealed class SettingsUiEffect {
    data object NavigateBack : SettingsUiEffect()
    data object OpenOssLicenses : SettingsUiEffect()
    data object OpenNotificationSettings : SettingsUiEffect()
    data object OpenExactAlarmSettings : SettingsUiEffect()
    data object RequestPostNotificationsPermission : SettingsUiEffect()
    data object RequestExactAlarmPermission : SettingsUiEffect()
}
