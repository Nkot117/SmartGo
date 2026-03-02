package com.nkot117.smartgo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.nkot117.core.domain.usecase.reminder.SyncReminderPermissionOnAppStartUseCase
import com.nkot117.core.ui.theme.SmartGoTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var syncReminderPermissionOnAppStartUseCase: SyncReminderPermissionOnAppStartUseCase

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
    }
}

fun isNotificationPermissionGranted(context: Context): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

fun isExactAlarmPermissionGranted(context: Context): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        alarmManager.canScheduleExactAlarms()
    } else {
        true
    }

@Composable
fun AppContent() {
    SmartGoTheme {
        AppScaffold()
    }
}
