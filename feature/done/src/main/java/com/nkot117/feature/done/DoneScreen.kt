package com.nkot117.feature.done

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.nkot117.core.navigation.DoneScreenTransitionParams
import com.nkot117.core.ui.components.PrimaryButton
import com.nkot117.core.ui.theme.BackgroundColor
import com.nkot117.core.ui.theme.SmartGoTheme

@Composable
fun DoneScreenRoute(
    contentPadding: PaddingValues,
    params: DoneScreenTransitionParams,
    onTapHome: () -> Unit
) {
    DoneScreen(
        contentPadding = contentPadding,
        checkedCount = params.checkedCount,
        totalCount = params.totalCount,
        onTapHome = onTapHome
    )
}

@Composable
fun DoneScreen(
    contentPadding: PaddingValues,
    checkedCount: Int,
    totalCount: Int,
    onTapHome: () -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .background(
                BackgroundColor
            )
    ) {
        val composition by rememberLottieComposition(
            LottieCompositionSpec.RawRes(com.nkot117.core.ui.R.raw.celebration)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LottieAnimation(
                composition = composition,
                iterations = 1,
                modifier = Modifier.size(140.dp)
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "忘れ物なし！",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "今日も安心していってらっしゃい",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ) {
                Text(
                    text = "今日のチェック完了：$checkedCount/$totalCount",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(80.dp))
        }

        PrimaryButton(
            text = "ホーム画面へ戻る",
            onClick = onTapHome,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 40.dp)
                .padding(horizontal = 41.dp)
                .height(56.dp)
                .widthIn(max = 360.dp)
                .semantics {
                    contentDescription = "back_home_button"
                }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DoneScreenPreview_Workday() {
    SmartGoTheme {
        DoneScreen(
            contentPadding = PaddingValues(0.dp),
            onTapHome = {},
            checkedCount = 10,
            totalCount = 10
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DoneScreenPreview_Holiday() {
    SmartGoTheme {
        DoneScreen(
            contentPadding = PaddingValues(0.dp),
            onTapHome = {},
            checkedCount = 10,
            totalCount = 10
        )
    }
}
