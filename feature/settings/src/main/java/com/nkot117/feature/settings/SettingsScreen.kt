package com.nkot117.feature.settings

import android.annotation.SuppressLint
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nkot117.core.domain.model.Reminder
import com.nkot117.core.ui.components.PrimaryButton
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
    // メインコンテンツの表示
    SettingsScreen(
        contentPadding,
        state
    ) { viewModel.onEvent(it) }

    // ダイアログの表示
    SettingsDialogs(
        state = state,
        onEvent = { viewModel.onEvent(it) }
    )

    // 副作用
    SettingsEffects(
        viewModel = viewModel,
        onBack = onBack,
        onTapOssLicenses = onTapOssLicenses
    )
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
