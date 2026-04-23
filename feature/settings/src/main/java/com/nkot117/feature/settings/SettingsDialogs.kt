package com.nkot117.feature.settings

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.nkot117.core.ui.components.PrimaryButton
import com.nkot117.core.ui.components.SecondaryButton

@Composable
fun SettingsDialogs(state: SettingsUiState, onEvent: (SettingsUiEvent) -> Unit) {
    when (state.dialog) {
        is SettingsDialog.ReminderTimePicker -> {
            NotificationTimePickerDialog(
                onEvent = { onEvent(it) },
                settingHour = state.reminder.hour,
                settingMinute = state.reminder.minute
            )
        }

        is SettingsDialog.NotificationRequiredDialog -> {
            NotificationRequiredDialog(
                onEvent = onEvent
            )
        }

        is SettingsDialog.ExactAlarmRequiredDialog -> {
            ExactAlarmRequiredDialog(
                onEvent = onEvent
            )
        }

        is SettingsDialog.LocationRequiredDialog -> {
            LocationRequiredDialog(
                onEvent = onEvent
            )
        }

        else -> {
            // No dialog to show
        }
    }
}

@Composable
private fun NotificationRequiredDialog(onEvent: (SettingsUiEvent) -> Unit) {
    AlertDialog(
        onDismissRequest = {
            onEvent(DialogEvent.NotificationRequiredDialogDismissed)
        },
        title = {
            Text("通知の許可が必要です")
        },
        text = {
            Text("リマインダー通知を受け取るには、設定画面で通知を許可してください。")
        },
        confirmButton = {
            PrimaryButton(
                onClick = {
                    onEvent(DialogEvent.NotificationRequiredDialogConfirmed)
                },
                text = "設定を開く"
            )
        },
        dismissButton = {
            SecondaryButton(
                onClick = {
                    onEvent(DialogEvent.NotificationRequiredDialogDismissed)
                },
                text = "キャンセル"
            )
        }
    )
}

@Composable
private fun ExactAlarmRequiredDialog(onEvent: (SettingsUiEvent) -> Unit) {
    AlertDialog(
        onDismissRequest = {
            onEvent(DialogEvent.ExactAlarmRequiredDialogDismissed)
        },
        title = {
            Text("アラームの許可が必要です")
        },
        text = {
            Text("リマインダー通知を受け取るには、設定画面でアラームを許可してください。")
        },
        confirmButton = {
            PrimaryButton(
                onClick = {
                    onEvent(DialogEvent.ExactAlarmRequiredDialogConfirmed)
                },
                text = "設定を開く"
            )
        },
        dismissButton = {
            SecondaryButton(
                onClick = {
                    onEvent(DialogEvent.ExactAlarmRequiredDialogDismissed)
                },
                text = "キャンセル"
            )
        }
    )
}

@Composable
private fun LocationRequiredDialog(onEvent: (SettingsUiEvent) -> Unit) {
    AlertDialog(
        onDismissRequest = {
            onEvent(DialogEvent.LocationPermissionDialogDismissed)
        },
        title = {
            Text("位置情報取得の許可が必要です")
        },
        text = {
            Text("自動天気設定を利用するには、設定画面で位置情報の許可してください。")
        },
        confirmButton = {
            PrimaryButton(
                onClick = {
                    onEvent(DialogEvent.LocationPermissionDialogConfirmed)
                },
                text = "設定を開く"
            )
        },
        dismissButton = {
            SecondaryButton(
                onClick = {
                    onEvent(DialogEvent.LocationPermissionDialogDismissed)
                },
                text = "キャンセル"
            )
        }
    )
}
