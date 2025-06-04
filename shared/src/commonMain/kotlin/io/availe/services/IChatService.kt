package io.availe.services

import arrow.core.Either
import io.availe.models.BranchId
import io.availe.models.InternalMessage
import io.availe.models.Session
import kotlinx.rpc.RemoteService
import kotlinx.rpc.annotations.Rpc

@Rpc
interface IChatService : RemoteService {
    // Sessions
    suspend fun getAllSessionIds(): Either<ApiError, List<String>>
    suspend fun getSession(sessionId: String): Either<ApiError, Session>
    suspend fun createSession(): Either<ApiError, Session>
    suspend fun deleteSession(sessionId: String): Either<ApiError, Unit>
    suspend fun updateSessionTitle(sessionId: String, title: String): Either<ApiError, Unit>

    // Branches
    suspend fun getBranchIds(sessionId: String): Either<ApiError, List<BranchId>>

    // Messages
    suspend fun getMessageIds(sessionId: String, branchId: BranchId): Either<ApiError, List<String>>
    suspend fun getMessages(sessionId: String, branchId: BranchId): Either<ApiError, List<InternalMessage>>
    suspend fun getMessage(sessionId: String, branchId: BranchId, messageId: String): Either<ApiError, InternalMessage>
    suspend fun addMessage(
        sessionId: String,
        branchId: BranchId,
        message: InternalMessage,
        forkBranch: Boolean = false
    ): Either<ApiError, BranchId>

    suspend fun updateMessage(sessionId: String, branchId: BranchId, message: InternalMessage): Either<ApiError, Unit>
    suspend fun deleteMessage(
        sessionId: String,
        branchId: BranchId,
        messageId: String,
        updateTimestamp: Long
    ): Either<ApiError, Unit>
}
