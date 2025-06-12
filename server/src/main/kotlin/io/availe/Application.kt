package io.availe

import io.availe.config.NetworkConfig
import io.availe.config.configurePlugins
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*

fun main() {
    dotenv { ignoreIfMissing = true }

    embeddedServer(
        CIO,
        port = NetworkConfig.SELF_PORT,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    configurePlugins()

//    val dsl = DatabaseFactory.createDsl(environment)
//    val conversationRepo = ConversationRepository(dsl)
//
//    val httpClient = HttpClientProvider.httpClient
//    val internalChat = OllamaClient(httpClient)
//    val externalChat = NLIPClient(httpClient, Url("http://localhost:8004"))
//    routing {
//        rpcServerConfig { serialization { json(Json { prettyPrint = true }) } }
//        rpc("/krpc/chat") {
//        }
//    }
}