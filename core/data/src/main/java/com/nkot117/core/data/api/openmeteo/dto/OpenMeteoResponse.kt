package com.nkot117.core.data.api.openmeteo.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenMeteoResponse(
    val latitude: Double,
    val longitude: Double,
    @SerialName("generationtime_ms")
    val generationTimeMs: Double,
    val timezone: String,
    @SerialName("timezone_abbreviation")
    val timezoneAbbreviation: String,
    val daily: Daily
)

@Serializable
data class Daily(
    val time: List<String>,
    @SerialName("weather_code")
    val weatherCode: List<Int>
)
