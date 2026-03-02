package com.nkot117.feature.settings

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun SettingsEffects(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onTapOssLicenses: () -> Unit
) {
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
