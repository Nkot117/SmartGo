package com.nkot117.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface AppNavKey : NavKey {
    @Serializable
    data object Home : AppNavKey

    @Serializable
    data class Checklist(val params: ChecklistScreenTransitionParams) : AppNavKey

    @Serializable
    data class Done(val params: DoneScreenTransitionParams) : AppNavKey

    @Serializable
    data object Items : AppNavKey

    @Serializable
    data object Setting : AppNavKey

    @Serializable
    data object OssLicenses : AppNavKey
}

@Serializable
data class ChecklistScreenTransitionParams(
    val dayType: NavDayType,
    val weatherType: NavWeatherType,
    val date: String
)

@Serializable
data class DoneScreenTransitionParams(val checkedCount: Int, val totalCount: Int)

@Serializable
enum class NavWeatherType { SUNNY, RAINY }

@Serializable
enum class NavDayType { WORKDAY, HOLIDAY }
