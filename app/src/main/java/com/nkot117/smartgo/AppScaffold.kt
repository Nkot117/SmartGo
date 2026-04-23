package com.nkot117.smartgo

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.nkot117.core.navigation.AppNavKey
import com.nkot117.core.navigation.Navigator
import com.nkot117.core.ui.components.AppTopBar
import com.nkot117.core.ui.theme.Primary500
import com.nkot117.feature.checklist.ChecklistScreenRoute
import com.nkot117.feature.done.DoneScreenRoute
import com.nkot117.feature.home.HomeScreenRoute
import com.nkot117.feature.items.ItemsScreenRoute
import com.nkot117.feature.settings.OssLicensesScreen
import com.nkot117.feature.settings.SettingsScreenRoute

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AppScaffold() {
    val navigator = remember { Navigator() }
    val backStack = navigator.backStack
    val scaffoldSpec = scaffoldSpecForNavKey(navigator.current, navigator = navigator)

    Scaffold(
        topBar = scaffoldSpec.topBar,
        floatingActionButton = scaffoldSpec.fab
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            onBack = { navigator.pop() },
            entryProvider =
            entryProvider {
                // HOME画面
                entry(AppNavKey.Home) {
                    HomeScreenRoute(
                        contentPadding = innerPadding,
                        transitionChecklistScreen = { checklistScreenTransitionParams ->
                            navigator.push(
                                AppNavKey.Checklist(
                                    params = checklistScreenTransitionParams
                                )
                            )
                        }
                    )
                }

                // チェックリスト画面
                entry<AppNavKey.Checklist> { key ->
                    val params = key.params
                    ChecklistScreenRoute(
                        contentPadding = innerPadding,
                        params = params,
                        onTapDone = { doneScreenTransitionParams ->
                            navigator.push(
                                AppNavKey.Done(params = doneScreenTransitionParams)
                            )
                        }
                    )
                }

                // 完了画面
                entry<AppNavKey.Done> { key ->
                    val params = key.params
                    DoneScreenRoute(
                        contentPadding = innerPadding,
                        params = params,
                        onTapHome = {
                            navigator.resetToHome()
                        }
                    )
                }

                // 持ち物登録画面
                entry<AppNavKey.Items> { key ->
                    ItemsScreenRoute(
                        contentPadding = innerPadding
                    )
                }

                // 設定画面
                entry(AppNavKey.Setting) { key ->
                    SettingsScreenRoute(
                        contentPadding = innerPadding,
                        onBack = { navigator.pop() },
                        onTapOssLicenses = {
                            navigator.push(AppNavKey.OssLicenses)
                        }
                    )
                }

                entry(AppNavKey.OssLicenses) { key ->
                    OssLicensesScreen(
                        aboutLibrariesRes = R.raw.aboutlibraries,
                        contentPadding = innerPadding
                    )
                }
            }
        )
    }
}

private fun scaffoldSpecForNavKey(appNavKey: AppNavKey, navigator: Navigator): ScaffoldSpec =
    when (appNavKey) {
        AppNavKey.Home ->
            ScaffoldSpec(
                topBar = {
                    AppTopBar(
                        title = "ホーム",
                        onAction = { navigator.push(AppNavKey.Setting) },
                        actionIcon = Icons.Default.Settings
                    )
                },
                fab = {
                    FloatingActionButton(
                        modifier =
                        Modifier
                            .padding(end = 16.dp, bottom = 96.dp)
                            .semantics { contentDescription = "add_item_fab" },
                        containerColor = Primary500,
                        onClick = {
                            navigator.push(AppNavKey.Items)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                    }
                }
            )

        is AppNavKey.Checklist ->
            ScaffoldSpec(
                topBar = { AppTopBar(title = "チェックリスト", onBack = { navigator.pop() }) }
            )

        is AppNavKey.Done ->
            ScaffoldSpec(
                topBar = { AppTopBar(title = "完了") }
            )

        AppNavKey.Items ->
            ScaffoldSpec(
                topBar = { AppTopBar(title = "持ち物の登録", onBack = { navigator.pop() }) }
            )

        AppNavKey.Setting ->
            ScaffoldSpec(
                topBar = {
                    AppTopBar(
                        title = "設定画面",
                        onBack = { navigator.pop() }
                    )
                }
            )

        AppNavKey.OssLicenses ->
            ScaffoldSpec(
                topBar = {
                    AppTopBar(
                        title = "OSSライセンス",
                        onBack = { navigator.pop() }
                    )
                }
            )
    }

data class ScaffoldSpec(
    val topBar: @Composable () -> Unit = {},
    val fab: @Composable () -> Unit = {}
)
