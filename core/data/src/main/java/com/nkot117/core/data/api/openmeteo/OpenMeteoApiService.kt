package com.nkot117.core.data.api.openmeteo

import com.nkot117.core.data.api.ApiClient
import com.nkot117.core.data.api.openmeteo.dto.OpenMeteoRequest
import com.nkot117.core.data.api.openmeteo.dto.OpenMeteoResponse
import io.ktor.client.request.parameter

class OpenMeteoApiService(private val apiClient: ApiClient) {
    suspend fun getDailyWeatherInfo(openMeteoRequest: OpenMeteoRequest): OpenMeteoResponse =
        apiClient.get("$BASE_URL$FORECAST_PATH") {
            parameter("latitude", openMeteoRequest.latitude)
            parameter("longitude", openMeteoRequest.longitude)
            parameter("forecast_days", openMeteoRequest.forecastDays)
            parameter("daily", openMeteoRequest.daily.joinToString(","))
            parameter("timezone", openMeteoRequest.timezone)
        }

    companion object {
        private const val BASE_URL = "https://api.open-meteo.com"
        private const val FORECAST_PATH = "/v1/forecast"
    }
}
