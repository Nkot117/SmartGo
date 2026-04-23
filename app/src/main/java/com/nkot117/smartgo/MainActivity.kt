package com.nkot117.smartgo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.nkot117.core.domain.usecase.reminder.SyncReminderPermissionOnAppStartUseCase
import com.nkot117.core.domain.usecase.weather.SyncLocationPermissionOnAppStartUseCase
import com.nkot117.core.ui.theme.SmartGoTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var syncReminderPermissionOnAppStartUseCase: SyncReminderPermissionOnAppStartUseCase

    @Inject
    lateinit var syncLocationPermissionOnAppStartUseCase: SyncLocationPermissionOnAppStartUseCase

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppContent()
        }
    }

    override fun onResume() {
        super.onResume()

        val hasNotificationPermission = isNotificationPermissionGranted(this)
        val hasExactAlarmPermission = isExactAlarmPermissionGranted(this)
        val permissionGranted = hasNotificationPermission && hasExactAlarmPermission
        lifecycleScope.launch {
            syncReminderPermissionOnAppStartUseCase(permissionGranted)
        }

        val hasLocationPermission = isLocationPermissionGranted(this)
        lifecycleScope.launch {
            syncLocationPermissionOnAppStartUseCase(hasLocationPermission)
        }
    }
}

private fun isNotificationPermissionGranted(context: Context): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

private fun isExactAlarmPermissionGranted(context: Context): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        alarmManager.canScheduleExactAlarms()
    } else {
        true
    }

private fun isLocationPermissionGranted(context: Context): Boolean {
    val fineLocationGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val coarseLocationGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    return fineLocationGranted || coarseLocationGranted
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AppContent() {
    SmartGoTheme {
        AppScaffold()
    }
}
