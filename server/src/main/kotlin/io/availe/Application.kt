package io.availe

import io.availe.client.NLIPClient
import io.availe.client.OllamaClient
import io.availe.config.DatabaseFactory
import io.availe.config.HttpClientProvider
import io.availe.config.NetworkConfig
import io.availe.config.configurePlugins
import io.availe.repositories.ConversationRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*
import kotlinx.rpc.krpc.ktor.server.rpc
import kotlinx.rpc.krpc.rpcServerConfig
import kotlinx.rpc.krpc.serialization.json.json
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(
        CIO,
        port = NetworkConfig.SELF_PORT,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    configurePlugins()

    val dsl = DatabaseFactory.createDsl(environment)
    val conversationRepo = ConversationRepository(dsl)

    val httpClient = HttpClientProvider.httpClient
    val internalChat = OllamaClient(httpClient)
    val externalChat = NLIPClient(httpClient, Url("http://localhost:8004"))
    routing {
        rpcServerConfig { serialization { json(Json { prettyPrint = true }) } }
        rpc("/krpc/chat") {
        }
    }
}
