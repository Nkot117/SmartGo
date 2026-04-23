package com.nkot117.core.data.api.openmeteo.dto

data class OpenMeteoRequest(
    val latitude: Double,
    val longitude: Double,
    val forecastDays: Int,
    val daily: List<String>,
    val timezone: String
)
