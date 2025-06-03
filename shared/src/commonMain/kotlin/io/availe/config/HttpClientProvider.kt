package io.availe.config

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.rpc.krpc.ktor.client.installKrpc

expect val httpClientEngine: HttpClientEngineFactory<*>

object HttpClientProvider {
    val httpClient: HttpClient = HttpClient(httpClientEngine) {
        install(ContentNegotiation) {
            json()
        }
    }

    val rpcClient: HttpClient = HttpClient(httpClientEngine) {
        install(ContentNegotiation) {
            json()
        }
        installKrpc {
            waitForServices = true
        }
    }
}
