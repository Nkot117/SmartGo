package com.nkot117.core.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.post

class ApiClient(@PublishedApi internal val httpClient: HttpClient) {
    suspend inline fun <reified T> get(url: String, block: HttpRequestBuilder.() -> Unit = {}): T =
        httpClient.get(url, block).body()

    suspend inline fun <reified T> post(url: String, block: HttpRequestBuilder.() -> Unit = {}): T =
        httpClient.post(url, block).body()
}
