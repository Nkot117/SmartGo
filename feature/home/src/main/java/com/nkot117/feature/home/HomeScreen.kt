package com.nkot117.feature.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.nkot117.core.common.toEpochMillis
import com.nkot117.core.domain.model.DayType
import com.nkot117.core.domain.model.Item
import com.nkot117.core.domain.model.ItemCategory
import com.nkot117.core.domain.model.WeatherType
import com.nkot117.core.navigation.ChecklistScreenTransitionParams
import com.nkot117.core.navigation.toNav
import com.nkot117.core.ui.components.AppTopBar
import com.nkot117.core.ui.components.CenterLoading
import com.nkot117.core.ui.components.ChecklistPreviewRow
import com.nkot117.core.ui.components.DatePickerField
import com.nkot117.core.ui.components.PrimaryButton
import com.nkot117.core.ui.components.SecondaryButton
import com.nkot117.core.ui.components.SegmentOption
import com.nkot117.core.ui.components.TwoOptionSegment
import com.nkot117.core.ui.theme.BackgroundColor
import com.nkot117.core.ui.theme.SmartGoTheme
import com.nkot117.core.ui.theme.TextSub
import java.time.ZoneOffset
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun HomeScreenRoute(
    contentPadding: PaddingValues,
    transitionChecklistScreen: (params: ChecklistScreenTransitionParams) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // メインコンテンツの表示
    HomeScreen(
        contentPadding = contentPadding,
        state = state,
        onEvent = viewModel::onEvent
    )

    // ダイアログの表示
    when (state.dialog) {
        HomeDialog.DailyNoteEditDialog -> {
            DailyNoteEditModal(
                draftNoteText = state.dailyNote,
                onEvent = viewModel::onEvent
            )
        }

        HomeDialog.AutoWeatherSettingsErrorDialog -> {
            AutoWeatherSettingsErrorDialog(
                onEvent = viewModel::onEvent
            )
        }

        null -> {
            // No dialog to show
        }
    }

    // 副作用
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                HomeUiEffect.TransitionToChecklistScreen -> {
                    val params = ChecklistScreenTransitionParams(
                        dayType = state.dayType.toNav(),
                        weatherType = state.weatherType.toNav(),
                        date = state.date.toString()
                    )
                    transitionChecklistScreen(params)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    contentPadding: PaddingValues,
    state: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit
) {
    val currentDailyNote = state.dailyNote

    Box(
        Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .background(
                BackgroundColor
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                start = 41.dp,
                end = 41.dp,
                top = 16.dp,
                bottom = 88.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                ConditionSection(state = state, onEvent = onEvent)

                Spacer(modifier = Modifier.padding(top = 15.dp))

                // 今日のノート
                DailyNoteCard(
                    text = currentDailyNote,
                    onClick = {
                        onEvent(ClickEvent.DailyNoteClicked)
                    }
                )

                Spacer(modifier = Modifier.padding(top = 15.dp))

                // 持ち物プレビュー
                ItemPreview(state.preview)

                Spacer(modifier = Modifier.padding(top = 30.dp))
            }
        }

        // チェックリスト遷移ボタン
        PrimaryButton(
            text = "チェックリストへ",
            onClick = {
                onEvent(ClickEvent.ChecklistClicked)
            },
            enabled = state.preview.isNotEmpty(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 40.dp)
                .padding(horizontal = 41.dp)
                .height(56.dp)
                .widthIn(max = 360.dp)
                .semantics { contentDescription = "go_to_checklist_button" }
        )
    }

    if (state.isLoadingWeather) {
        CenterLoading()
    }
}

@Composable
private fun ItemPreview(previewList: ImmutableList<Item>) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "持ち物プレビュー",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.padding(top = 5.dp))

            if (previewList.isEmpty()) {
                Text(
                    "プレビューできる持ち物がありません。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSub,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    "持ち物は＋ボタンから追加できます。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSub,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                previewList.forEach {
                    key(it.id) {
                        ChecklistPreviewRow(
                            title = it.name
                        )
                        Spacer(modifier = Modifier.padding(top = 15.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ConditionSection(state: HomeUiState, onEvent: (HomeUiEvent) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 日付選択
        DatePickerField(
            selectedDateMillis = state.date.toEpochMillis(ZoneOffset.UTC),
            onDateChange = {
                onEvent(DialogEvent.CalendarDialogConfirmed(it))
            },
            confirmButtonLabel = "OK",
            cancelButtonLabel = "キャンセル",
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(20.dp))

        // 天気アイコン
        WeatherIcon(weatherType = state.weatherType)
    }

    Spacer(modifier = Modifier.padding(top = 15.dp))

    // Work / Holiday 選択
    TwoOptionSegment(
        left = SegmentOption(DayType.WORKDAY, "仕事の日"),
        right = SegmentOption(DayType.HOLIDAY, "休みの日"),
        selected = state.dayType,
        onSelectedChange = {
            onEvent(ClickEvent.DailyTypeToggled(it))
        },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.padding(top = 15.dp))

    // Sunny / Rainy 選択
    TwoOptionSegment(
        left = SegmentOption(WeatherType.SUNNY, "晴れの日"),
        right = SegmentOption(WeatherType.RAINY, "雨の日"),
        selected = state.weatherType,
        onSelectedChange = {
            onEvent(ClickEvent.WeatherTypeToggled(it))
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun DailyNoteCard(text: String, onClick: () -> Unit) {
    val displayText = text.ifBlank { "タップして今日のメモを追加" }

    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.large)
        ) {
            Text(
                text = "今日のノート",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = displayText,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 4
            )
        }
    }
}

@Composable
private fun WeatherIcon(weatherType: WeatherType) {
    AnimatedContent(
        targetState = weatherType,
        transitionSpec = {
            val direction = if (targetState == WeatherType.RAINY) 1 else -1
            val slideIn = slideInHorizontally(
                animationSpec = tween(320, easing = FastOutSlowInEasing),
                initialOffsetX = { fullWidth -> fullWidth * direction }
            )
            val slideOut = slideOutHorizontally(
                animationSpec = tween(260, easing = FastOutSlowInEasing),
                targetOffsetX = { fullWidth -> -fullWidth * direction }
            )

            (slideIn + fadeIn(tween(320))) togetherWith
                (slideOut + fadeOut(tween(200)))
        },
        label = "weather_lottie_switch"
    ) { type ->
        val resId = if (type == WeatherType.SUNNY) {
            com.nkot117.core.ui.R.raw.sunny
        } else {
            com.nkot117.core.ui.R.raw.rainy
        }

        val composition by rememberLottieComposition(
            LottieCompositionSpec.RawRes(
                resId
            )
        )

        key(type) {
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(54.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DailyNoteEditModal(draftNoteText: String, onEvent: (HomeUiEvent) -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var draftNoteText by remember { mutableStateOf(draftNoteText) }

    ModalBottomSheet(
        onDismissRequest = {
            onEvent(DialogEvent.DailyNoteEditDialogDismissed)
        },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "今日のノート",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = draftNoteText,
                onValueChange = { draftNoteText = it },
                minLines = 4,
                placeholder = {
                    Text("例：ティッシュ切れてるので帰りに買う")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                SecondaryButton(
                    text = "キャンセル",
                    onClick = { onEvent(DialogEvent.DailyNoteEditDialogDismissed) }
                )

                Spacer(modifier = Modifier.width(8.dp))

                PrimaryButton(
                    text = "保存",
                    onClick = {
                        onEvent(DialogEvent.DailyNoteEditDialogConfirmed(draftNoteText))
                    },
                    enabled = draftNoteText.isNotBlank()
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun AutoWeatherSettingsErrorDialog(onEvent: (HomeUiEvent) -> Unit) {
    AlertDialog(
        onDismissRequest = {
            onEvent(DialogEvent.AutoWeatherSettingsErrorDialogDismissed)
        },
        title = {
            Text("天気情報の取得に失敗しました")
        },
        text = {
            Text("天気情報は手動で設定してください。")
        },
        confirmButton = {
            PrimaryButton(
                onClick = {
                    onEvent(DialogEvent.AutoWeatherSettingsErrorDialogDismissed)
                },
                text = "OK"
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    SmartGoTheme {
        Scaffold(
            topBar = { AppTopBar("ホーム") },
            floatingActionButton = {
                FloatingActionButton(onClick = {}) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null
                    )
                }
            }
        ) { inner ->
            HomeScreen(
                contentPadding = inner,
                state = HomeUiState(
                    preview = persistentListOf(
                        Item(
                            name = "家の鍵",
                            category = ItemCategory.ALWAYS
                        ),
                        Item(
                            name = "財布",
                            category = ItemCategory.ALWAYS
                        ),
                        Item(
                            name = "スマートフォン",
                            category = ItemCategory.ALWAYS
                        ),
                        Item(
                            name = "社員証",
                            category = ItemCategory.WORKDAY
                        ),
                        Item(
                            name = "折りたたみ傘",
                            category = ItemCategory.RAINY
                        )
                    )
                ),
                onEvent = {}
            )
        }
    }
}
