//package io.availe.config
//
//import io.availe.services.IChatService
//import io.availe.util.SuspendLazy
//import io.ktor.client.*
//import io.ktor.client.engine.*
//import io.ktor.client.plugins.contentnegotiation.*
//import io.ktor.http.*
//import io.ktor.serialization.kotlinx.json.*
//import kotlinx.rpc.krpc.ktor.client.installKrpc
//import kotlinx.rpc.krpc.ktor.client.rpc
//import kotlinx.rpc.krpc.serialization.json.json
//import kotlinx.rpc.withService
//import kotlinx.serialization.json.Json
//
//expect val httpClientEngine: HttpClientEngineFactory<*>
//
//object HttpClientProvider {
//    val httpClient: HttpClient = HttpClient(httpClientEngine) {
//        install(ContentNegotiation) {
//            json()
//        }
//    }
//
//    val krpcClient: HttpClient = HttpClient(httpClientEngine) {
//        installKrpc {
//            serialization {
//                json(Json { prettyPrint = true })
//            }
//        }
//    }
//
//    private val chatServiceLazy = SuspendLazy {
//        val wsUrl = URLBuilder(
//            protocol = if (NetworkConfig.serverUrl.protocol.name == "https") URLProtocol.WSS else URLProtocol.WS,
//            host = NetworkConfig.serverUrl.host,
//            port = NetworkConfig.serverUrl.port
//        ).apply {
//            encodedPath = "/krpc/chat"
//        }.buildString()
//
//        val rawRpc = krpcClient.rpc(wsUrl) {}
//        rawRpc.withService<IChatService>()
//    }
//
//    suspend fun createChatService(): IChatService = chatServiceLazy.get()
//}
