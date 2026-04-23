package com.nkot117.feature.settings

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Suppress("CyclomaticComplexMethod")
@Composable
fun SettingsEffects(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onTapOssLicenses: () -> Unit
) {
    val context = LocalContext.current

    val requestPostNotificationsPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.onEvent(PermissionEvent.PostNotifications(granted = true))
        } else {
            viewModel.onEvent(PermissionEvent.PostNotifications(granted = false))
        }
    }

    val requestLocationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        val fineLocationGranted = granted[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = granted[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            viewModel.onEvent(PermissionEvent.Location(granted = true))
        } else {
            viewModel.onEvent(PermissionEvent.Location(granted = false))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                SettingsUiEffect.NavigateBack -> onBack()

                SettingsUiEffect.OpenOssLicenses -> onTapOssLicenses()

                SettingsUiEffect.OpenNotificationSettings -> openNotificationSettings(
                    context
                )

                SettingsUiEffect.OpenLocationSettings -> openLocationSettings(context)

                SettingsUiEffect.RequestPostNotificationsPermission -> {
                    requestPostNotificationPermission(
                        context,
                        viewModel,
                        requestPostNotificationsPermission
                    )
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

                SettingsUiEffect.RequestLocationPermission -> {
                    requestLocationPermission(context, viewModel, requestLocationPermission)
                }
            }
        }
    }
}

private fun requestPostNotificationPermission(
    context: Context,
    viewModel: SettingsViewModel,
    requestPostNotificationsPermission: ActivityResultLauncher<String>
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        // Android 13未満は通知権限の許諾は不要なため、許可されたものとして扱う
        viewModel.onEvent(PermissionEvent.PostNotifications(granted = true))
        return
    }

    val hasPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED

    if (hasPermission) {
        viewModel.onEvent(PermissionEvent.PostNotifications(granted = true))
        return
    } else {
        requestPostNotificationsPermission.launch(
            Manifest.permission.POST_NOTIFICATIONS
        )
    }
}

private fun requestLocationPermission(
    context: Context,
    viewModel: SettingsViewModel,
    requestLocationPermission: ActivityResultLauncher<Array<String>>
) {
    val hasFineLocationPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    if (hasFineLocationPermission || hasCoarseLocationPermission) {
        // すでに位置情報の権限が許可されている場合は、イベントを送信して設定を保存する
        viewModel.onEvent(PermissionEvent.Location(granted = true))
    } else {
        // 位置情報の権限が許可されていない場合は、権限リクエストを開始する
        requestLocationPermission.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

        )
    }
}

private fun openNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

private fun openLocationSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
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
