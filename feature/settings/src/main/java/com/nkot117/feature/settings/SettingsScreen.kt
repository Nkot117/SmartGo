package com.nkot117.feature.settings

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nkot117.core.domain.model.Reminder
import com.nkot117.core.ui.components.PrimaryButton
import com.nkot117.core.ui.components.SecondaryButton
import com.nkot117.core.ui.theme.BgWorkdayBottom
import com.nkot117.core.ui.theme.BgWorkdayTop
import com.nkot117.core.ui.theme.Primary500
import com.nkot117.core.ui.theme.TextMain
import com.nkot117.core.ui.theme.TextSub

@Composable
fun SettingsScreenRoute(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onTapOssLicenses: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel<SettingsViewModel>()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val requestPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.onEvent(PermissionEvent.PostNotifications(granted = true))
        } else {
            viewModel.onEvent(PermissionEvent.PostNotifications(granted = false))
        }
    }

    // メインコンテンツの表示
    SettingsScreen(
        contentPadding,
        state
    ) { viewModel.onEvent(it) }

    // ダイアログの表示
    when (state.dialog) {
        is SettingsDialog.ReminderTimePicker -> {
            NotificationTimePickerDialog(
                onEvent = { viewModel.onEvent(it) },
                settingHour = state.reminder.hour,
                settingMinute = state.reminder.minute
            )
        }

        is SettingsDialog.NotificationRequiredDialog -> {
            NotificationRequiredDialog(
                onEvent = viewModel::onEvent
            )
        }

        is SettingsDialog.ExactAlarmRequiredDialog -> {
            ExactAlarmRequiredDialog(
                onEvent = viewModel::onEvent
            )
        }

        else -> {
            // No dialog to show
        }
    }

    // 副作用
    LaunchedEffect(Unit) {
        // 画面表示時にリマインダー設定をロードし、UIを初期化する
        viewModel.fetchReminderSettings()
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                SettingsUiEffect.NavigateBack -> onBack()

                SettingsUiEffect.OpenOssLicenses -> onTapOssLicenses()

                SettingsUiEffect.OpenNotificationSettings -> openNotificationSettings(
                    context
                )

                SettingsUiEffect.RequestPostNotificationsPermission -> {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                        // Android 13未満は通知権限の許諾は不要なため、許可されたものとして扱う
                        viewModel.onEvent(PermissionEvent.PostNotifications(granted = true))
                        return@collect
                    }

                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasPermission) {
                        viewModel.onEvent(PermissionEvent.PostNotifications(granted = true))
                        return@collect
                    } else {
                        requestPermission.launch(
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    }
                }

                SettingsUiEffect.RequestExactAlarmPermission -> {
                    val hasExactAlarmPermission = checkExactAlarmPermission(context)
                    if (hasExactAlarmPermission) {
                        viewModel.onEvent(PermissionEvent.ExactAlarm(granted = true))
                    } else {
                        viewModel.onEvent(PermissionEvent.ExactAlarm(granted = false))
                    }
                }

                SettingsUiEffect.OpenExactAlarmSettings -> openExactAlarmSettings(context)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    contentPadding: PaddingValues,
    state: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit
) {
    val topColor = BgWorkdayTop
    val bottomColor = BgWorkdayBottom

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(topColor, bottomColor)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            ReminderSettingsCard(
                reminderSettings = state.reminder,
                onEvent = onEvent
            )

            Spacer(Modifier.height(12.dp))

            OssLicensesCard(
                onEvent = onEvent
            )
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
private fun ReminderSettingsCard(reminderSettings: Reminder, onEvent: (SettingsUiEvent) -> Unit) {
    Text(
        "外出前リマインダー設定",
        style = MaterialTheme.typography.titleSmall,
        color = TextMain
    )

    Spacer(Modifier.height(8.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ReminderToggleRow(
                reminderSettings.enabled
            ) { enabled ->
                onEvent(ReminderEvent.ReminderToggled(enabled))
            }

            if (reminderSettings.enabled) {
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.LightGray)
                )
                Spacer(Modifier.height(16.dp))
                NotificationTimeRow(
                    { onEvent(ClickEvent.TimeClicked) },
                    reminderSettings.hour,
                    reminderSettings.minute
                )
            }
            Spacer(Modifier.height(16.dp))
            PrimaryButton(
                onClick = {
                    onEvent(ClickEvent.SaveClicked)
                },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = "保存する"
            )
        }
    }
}

@Composable
private fun OssLicensesCard(onEvent: (SettingsUiEvent) -> Unit) {
    Text(
        "その他",
        style = MaterialTheme.typography.titleSmall,
        color = TextMain
    )

    Spacer(Modifier.height(8.dp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onEvent(ClickEvent.OssLicensesClicked)
            },
        colors = cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "OSSライセンス",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
                color = TextMain
            )
            Text(
                text = "確認する",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSub
            )
        }
    }
}

@Composable
private fun ReminderToggleRow(isEnabled: Boolean, toggled: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "外出前リマインダー",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelLarge,
            color = TextMain
        )
        Switch(
            checked = isEnabled,
            onCheckedChange = {
                toggled(it)
            },
            colors = SwitchDefaults.colors(
                checkedTrackColor = Primary500
            )
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun NotificationTimeRow(timeClicked: () -> Unit, settingHour: Int, settingMinute: Int) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    timeClicked()
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "通知時刻",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
                color = TextMain
            )
            val hourText = settingHour.toString().padStart(2, '0')
            val minuteText = settingMinute.toString().padStart(2, '0')
            Text(
                "$hourText:$minuteText",
                style = MaterialTheme.typography.bodyLarge,
                color = TextMain
            )
        }

        Spacer(Modifier.height(5.dp))

        Text(
            "毎日この時刻に通知します",
            style = MaterialTheme.typography.bodySmall,
            color = TextSub,
            modifier = Modifier.padding(start = 0.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationTimePickerDialog(
    onEvent: (SettingsUiEvent) -> Unit,
    settingHour: Int,
    settingMinute: Int
) {
    val timePickerState = rememberTimePickerState(
        initialHour = settingHour,
        initialMinute = settingMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = {
            onEvent(ReminderEvent.ReminderTimePickerDismissed)
        },
        title = { Text("通知時刻を選択") },
        text = { TimePicker(state = timePickerState) },
        confirmButton = {
            Button(onClick = {
                onEvent(
                    ReminderEvent.ReminderTimePickerConfirmed(
                        timePickerState.hour,
                        timePickerState.minute
                    )
                )
            }) {
                Text("確定")
            }
        },
        dismissButton = {
            Button(onClick = {
                onEvent(ReminderEvent.ReminderTimePickerDismissed)
            }) {
                Text("キャンセル")
            }
        }
    )
}

private fun openNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    }
    context.startActivity(intent)
}

private fun openExactAlarmSettings(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        return
    }

    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
    context.startActivity(intent)
}

private fun checkExactAlarmPermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        // Android 12未満は正確なアラームの権限は存在しないため、常に許可されているものとする
        return true
    }

    val alarmManager = context.getSystemService(AlarmManager::class.java)
    return alarmManager.canScheduleExactAlarms()
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    Surface {
        SettingsScreen(
            contentPadding = PaddingValues(0.dp),
            state = SettingsUiState(),
            onEvent = {}
        )
    }
}
