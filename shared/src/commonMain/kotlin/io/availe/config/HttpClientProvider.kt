package io.availe.config

import io.availe.services.IChatService
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.rpc.krpc.ktor.client.installKrpc
import kotlinx.rpc.krpc.ktor.client.rpc
import kotlinx.rpc.krpc.serialization.json.json
import kotlinx.rpc.withService
import kotlinx.serialization.json.Json

expect val httpClientEngine: HttpClientEngineFactory<*>

object HttpClientProvider {
    val httpClient: HttpClient = HttpClient(httpClientEngine) {
        install(ContentNegotiation) {
            json()
        }
    }

    val krpcClient: HttpClient = HttpClient(httpClientEngine) {
        installKrpc {
            serialization {
                json(Json { prettyPrint = true })
            }
        }
    }

    suspend fun createChatService(): IChatService {
        val rawRpc = krpcClient.rpc("${NetworkConfig.serverUrl}/krpc/chat") {
        }
        return rawRpc.withService<IChatService>()
    }
}
