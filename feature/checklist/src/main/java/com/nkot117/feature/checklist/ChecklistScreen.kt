package com.nkot117.feature.checklist

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nkot117.core.navigation.ChecklistScreenTransitionParams
import com.nkot117.core.navigation.DoneScreenTransitionParams
import com.nkot117.core.navigation.toDomain
import com.nkot117.core.ui.components.AppTopBar
import com.nkot117.core.ui.components.ChecklistRow
import com.nkot117.core.ui.components.PrimaryButton
import com.nkot117.core.ui.components.SecondaryButton
import com.nkot117.core.ui.theme.BackgroundColor
import com.nkot117.core.ui.theme.ProgressActive
import com.nkot117.core.ui.theme.ProgressComplete
import com.nkot117.core.ui.theme.ProgressTrack
import com.nkot117.core.ui.theme.SmartGoTheme
import com.nkot117.core.ui.theme.TextSub
import java.time.LocalDate

@Composable
fun ChecklistScreenRoute(
    contentPadding: PaddingValues,
    params: ChecklistScreenTransitionParams,
    onTapDone: (params: DoneScreenTransitionParams) -> Unit,
    viewModel: ChecklistViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isAllChecked = viewModel.isAllChecked

    LaunchedEffect(Unit) {
        viewModel.getChecklist(
            dayType = params.dayType.toDomain(),
            weatherType = params.weatherType.toDomain(),
            date = LocalDate.parse(params.date)
        )
    }

    ChecklistScreen(
        contentPadding = contentPadding,
        checklist = state.checklist,
        toggleChecklistItem = viewModel::toggleChecklistItem,
        onCheckAll = viewModel::checkAllItems,
        isAllChecked = isAllChecked,
        onTapDone = onTapDone
    )
}

@Suppress("LongParameterList")
@Composable
fun ChecklistScreen(
    contentPadding: PaddingValues,
    checklist: List<ChecklistItem>,
    toggleChecklistItem: (id: Long, checked: Boolean) -> Unit,
    onCheckAll: () -> Unit,
    isAllChecked: Boolean,
    onTapDone: (params: DoneScreenTransitionParams) -> Unit
) {
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
                val checkedCount = checklist.count { it.checked }
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 進捗バー
                    ChecklistProgressHeader(
                        checkedCount = checkedCount,
                        totalCount = checklist.size
                    )

                    Spacer(Modifier.height(8.dp))

                    // 全てチェックボタン
                    SecondaryButton(
                        text = "すべてチェック",
                        onClick = onCheckAll,
                        modifier = Modifier.align(Alignment.End)
                    )
                }

                Spacer(Modifier.height(16.dp))
            }

            // チェックリスト
            items(
                items = checklist,
                key = { item -> item.id }
            ) { item ->
                ChecklistRow(
                    title = item.title,
                    checked = item.checked,
                    onToggle = {
                        toggleChecklistItem(
                            item.id,
                            !item.checked
                        )
                    }
                )

                Spacer(modifier = Modifier.padding(top = 15.dp))
            }
        }

        // 遷移ボタン
        PrimaryButton(
            text = "出発する",
            onClick = {
                val params = DoneScreenTransitionParams(
                    checkedCount = checklist.size,
                    totalCount = checklist.filter {
                        it.checked
                    }.size
                )
                onTapDone(params)
            },
            enabled = isAllChecked,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 40.dp)
                .padding(horizontal = 41.dp)
                .height(56.dp)
                .widthIn(max = 360.dp)
                .semantics { contentDescription = "departure_button" }
        )
    }
}

@Composable
fun ChecklistProgressHeader(checkedCount: Int, totalCount: Int, modifier: Modifier = Modifier) {
    val progress =
        if (totalCount == 0) {
            0f
        } else {
            checkedCount / totalCount.toFloat()
        }

    val text =
        if (checkedCount == totalCount && totalCount > 0) {
            "すべて完了"
        } else {
            "$checkedCount / $totalCount 完了"
        }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "進捗",
                style = MaterialTheme.typography.titleSmall,
                color = TextSub
            )

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSub
            )
        }

        Spacer(Modifier.height(8.dp))

        ChecklistProgressBar(progress = progress)
    }
}

@Composable
fun ChecklistProgressBar(progress: Float, modifier: Modifier = Modifier) {
    val safeProgress = progress.coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = safeProgress,
        label = "checklist_progress"
    )
    val barColor =
        if (progress >= 1f) {
            ProgressComplete
        } else {
            ProgressActive
        }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(12.dp))
            .drawBehind {
                drawRoundRect(
                    color = ProgressTrack,
                    cornerRadius = CornerRadius(
                        x = size.height / 2,
                        y = size.height / 2
                    )
                )

                drawRoundRect(
                    color = barColor,
                    size = size.copy(width = size.width * animatedProgress),
                    cornerRadius = CornerRadius(
                        x = size.height / 2,
                        y = size.height / 2
                    )
                )
            }
    )
}

@Preview(showBackground = true)
@Composable
private fun ChecklistScreenPreview() {
    SmartGoTheme {
        Scaffold(
            topBar = { AppTopBar("チェックリスト") }
        ) { inner ->
            ChecklistScreen(
                contentPadding = inner,
                checklist = listOf(
                    ChecklistItem(id = 1, title = "家の鍵", checked = true),
                    ChecklistItem(id = 2, title = "財布", checked = true),
                    ChecklistItem(id = 3, title = "スマートフォン", checked = false),
                    ChecklistItem(id = 4, title = "社員証", checked = false),
                    ChecklistItem(id = 5, title = "折りたたみ傘", checked = false)
                ),
                toggleChecklistItem = { _, _ -> },
                onCheckAll = {},
                isAllChecked = false,
                onTapDone = {}
            )
        }
    }
}
