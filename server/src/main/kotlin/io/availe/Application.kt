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
import kotlinx.coroutines.runBlocking
import kotlinx.rpc.krpc.ktor.server.rpc
import kotlinx.rpc.krpc.rpcServerConfig
import kotlinx.rpc.krpc.serialization.json.json
import kotlinx.serialization.json.Json
import kotlin.time.Duration
import kotlin.time.measureTime
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

    benchmarkFetchMethods(repository, ownerId)
}

fun benchmarkFetchMethods(repo: ConversationRepository, userId: UserId) {
    val fetchDuration: Duration = measureTime {
        val idsOpt = repo.fetchAllUserConversationIds(userId)
        val ids = idsOpt.getOrNull()
        ids?.forEach { conversationId ->
            repo.fetchConversationById(conversationId)
        }
    }
    println("Time taken for .fetch() + byId: $fetchDuration")

    val flowDuration: Duration = measureTime {
        runBlocking {
            repo.streamAllUserConversations(userId).collect { }
        }
    }
    println("Time taken for streaming Flow: $flowDuration")
}