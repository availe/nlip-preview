package io.availe

import io.availe.client.NLIPClient
import io.availe.client.OllamaClient
import io.availe.config.DatabaseFactory
import io.availe.config.HttpClientProvider
import io.availe.config.NetworkConfig
import io.availe.config.configurePlugins
import io.availe.models.*
import io.availe.repositories.ConversationRepository
import io.github.cdimascio.dotenv.dotenv
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*
import kotlinx.rpc.krpc.ktor.server.rpc
import kotlinx.rpc.krpc.rpcServerConfig
import kotlinx.rpc.krpc.serialization.json.json
import kotlinx.serialization.json.Json
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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

    val dsl = DatabaseFactory.createDsl(environment)
    val conversationRepo = ConversationRepository(dsl)

    hello(conversationRepo)

    val httpClient = HttpClientProvider.httpClient
    val internalChat = OllamaClient(httpClient)
    val externalChat = NLIPClient(httpClient, Url("http://localhost:8004"))
    routing {
        rpcServerConfig { serialization { json(Json { prettyPrint = true }) } }
        rpc("/krpc/chat") {
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
fun hello(repository: ConversationRepository) {
    val randomTitle = "Test Conversation"
    val ownerId = UserId.from(Uuid.parse("e9126d4e-4667-4409-92b3-1032e5f90150"))
    val create = ConversationCreateRequest(
        title = ConversationTitle(randomTitle),
        owner = ownerId,
        status = Conversation.Status.ACTIVE,
        version = ConversationSchemaVersion(1)
    )
    val conversation = repository.insertConversation(create)
    println("Inserted conversation: $conversation")

    printAllUserConversations(repository, ownerId)
}

fun printAllUserConversations(repo: ConversationRepository, userId: UserId) {
    val all = repo.fetchAllUserConversationIds(userId)
    if (all.isNone()) {
        println("No conversations found for user: $userId")
        printAllUserConversations(repo, userId)
        return
    }
    println("Conversations for user $userId:")
    all.getOrNull()?.forEach { conversationId ->
        val convOpt = repo.fetchConversationById(conversationId)
        println(convOpt)
    }
}
