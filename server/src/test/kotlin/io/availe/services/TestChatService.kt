package io.availe.services

import arrow.core.Either
import io.availe.models.Conversation
import io.availe.models.InternalMessage

class TestChatService {
    private val sessionStore = InMemorySessionStore()

    fun getSession(sessionIdentifier: String): Either<ChatError, Conversation> =
        sessionStore.get(sessionIdentifier)

    fun getAllSessionIdentifiers(): List<String> =
        sessionStore.getAllSessionIds()

    suspend fun createSession(conversation: Conversation): Either<ChatError, Unit> =
        sessionStore.tryInsert(conversation.copy(lastActivityAt = conversation.createdAt))

    suspend fun deleteSession(sessionIdentifier: String): Either<ChatError, Unit> =
        sessionStore.delete(sessionIdentifier)

    suspend fun sendMessage(
        sessionIdentifier: String,
        branchIdentifier: BranchId,
        message: InternalMessage
    ): Either<ChatError, Unit> =
        sessionStore.mutateSession(sessionIdentifier) {
            send(branchIdentifier, message)
        }

    suspend fun editMessage(
        sessionIdentifier: String,
        branchIdentifier: BranchId,
        message: InternalMessage,
        forkBranch: Boolean = false
    ): Either<ChatError, BranchId> =
        sessionStore.mutateSession(sessionIdentifier) {
            edit(branchIdentifier, message, forkBranch)
        }

    suspend fun deleteMessage(
        sessionIdentifier: String,
        branchIdentifier: BranchId,
        messageIdentifier: String,
        updateTimestamp: Long
    ): Either<ChatError, Unit> =
        sessionStore.mutateSession(sessionIdentifier) {
            delete(branchIdentifier, messageIdentifier, updateTimestamp)
        }

    suspend fun getBranchSnapshot(
        sessionIdentifier: String
    ): Either<ChatError, Map<BranchId, List<InternalMessage>>> =
        sessionStore.snapshotBranches(sessionIdentifier)
}
