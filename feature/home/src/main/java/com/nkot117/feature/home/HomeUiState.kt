package com.nkot117.feature.home

import com.nkot117.core.domain.model.DayType
import com.nkot117.core.domain.model.Item
import com.nkot117.core.domain.model.WeatherType
import java.time.LocalDate
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Home画面のUI状態を表すデータクラス
 */
data class HomeUiState(
    val isLoadingWeather: Boolean = false,
    val date: LocalDate = LocalDate.now(),
    val dayType: DayType = DayType.WORKDAY,
    val weatherType: WeatherType = WeatherType.SUNNY,
    val preview: ImmutableList<Item> = persistentListOf(),
    val dailyNote: String = "",
    val dialog: HomeDialog? = null
)

/**
 * Home画面で表示されるダイアログの種類を表すシールクラス
 */
sealed class HomeDialog {
    data object DailyNoteEditDialog : HomeDialog()
    data object AutoWeatherSettingsErrorDialog : HomeDialog()
}

/**
 * Home画面のUIイベントを表すシールインターフェース
 */
sealed interface HomeUiEvent

/**
 * クリックイベント
 */
sealed interface ClickEvent : HomeUiEvent {
    data class DailyTypeToggled(val dayType: DayType) : ClickEvent
    data class WeatherTypeToggled(val weatherType: WeatherType) : ClickEvent
    data object DailyNoteClicked : ClickEvent
    data object ChecklistClicked : ClickEvent
}

/**
 * ダイアログイベント
 */
sealed interface DialogEvent : HomeUiEvent {
    data class DailyNoteEditDialogConfirmed(val note: String) : DialogEvent
    data object DailyNoteEditDialogDismissed : DialogEvent
    data class CalendarDialogConfirmed(val selectedDate: Long) : DialogEvent
    data object CalendarDialogDismissed : DialogEvent
    data object AutoWeatherSettingsErrorDialogDismissed : DialogEvent
}

/**
 * Home画面で実行する副作用を表すシールクラス
 */
sealed class HomeUiEffect {
    data object TransitionToChecklistScreen : HomeUiEffect()
}
