package io.availe.services.impl

import arrow.core.Either
import io.availe.models.BranchId
import io.availe.models.Conversation
import io.availe.models.InternalMessage
import io.availe.services.*
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object ChatServiceImpl : IChatService {
    override suspend fun getAllSessionIds(): Either<ApiError, List<String>> =
        Either.catch {
            ChatStore.getAllSessionIdentifiers()
        }.mapLeft { e ->
            ApiError(
                type = ApiErrorType.SESSION_NOT_FOUND,
                message = e.message
            )
        }

    override suspend fun getSession(sessionId: String): Either<ApiError, Conversation> =
        ChatStore.getSession(sessionId).mapLeft { chatError: ChatError ->
            ApiError(
                type = ApiErrorType.SESSION_NOT_FOUND,
                message = chatError.toApiError().message
            )
        }

    @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
    override suspend fun createSession(): Either<ApiError, Conversation> {
        val currentTimestamp: Instant = Clock.System.now()
        val conversation = Conversation(
            id = Uuid.random().toString(),
            title = null,
            createdAt = currentTimestamp,
            lastActivityAt = currentTimestamp,
            participantIds = emptySet(),
            status = Conversation.Status.ACTIVE
        )
        return ChatStore.createSession(conversation)
            .mapLeft { chatError ->
                ApiError(
                    type = ApiErrorType.SESSION_CREATE_FAILED,
                    message = chatError.message
                )
            }
            .map { conversation }
    }

    override suspend fun deleteSession(sessionId: String): Either<ApiError, Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun updateSessionTitle(
        sessionId: String,
        title: String
    ): Either<ApiError, Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun getBranchIds(sessionId: String): Either<ApiError, List<BranchId>> {
        TODO("Not yet implemented")
    }

    override suspend fun getMessageIds(
        sessionId: String,
        branchId: BranchId
    ): Either<ApiError, List<String>> {
        TODO("Not yet implemented")
    }

    override suspend fun getMessages(
        sessionId: String,
        branchId: BranchId
    ): Either<ApiError, List<InternalMessage>> {
        TODO("Not yet implemented")
    }

    override suspend fun getMessage(
        sessionId: String,
        branchId: BranchId,
        messageId: String
    ): Either<ApiError, InternalMessage> {
        TODO("Not yet implemented")
    }

    override suspend fun addMessage(
        sessionId: String,
        branchId: BranchId,
        message: InternalMessage,
        forkBranch: Boolean
    ): Either<ApiError, BranchId> {
        TODO("Not yet implemented")
    }

    override suspend fun updateMessage(
        sessionId: String,
        branchId: BranchId,
        message: InternalMessage
    ): Either<ApiError, Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteMessage(
        sessionId: String,
        branchId: BranchId,
        messageId: String,
        updateTimestamp: Long
    ): Either<ApiError, Unit> {
        TODO("Not yet implemented")
    }

    override val coroutineContext: CoroutineContext = Dispatchers.Default
}