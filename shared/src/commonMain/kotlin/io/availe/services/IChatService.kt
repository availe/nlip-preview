package io.availe.services

import arrow.core.Either
import io.availe.models.Conversation
import kotlinx.rpc.RemoteService
import kotlinx.rpc.annotations.Rpc

@Rpc
interface IChatService : RemoteService {
    // Sessions
    suspend fun getAllSessionIds(): Either<ApiError, List<String>>
    suspend fun getSession(sessionId: String): Either<ApiError, Conversation>
    suspend fun createSession(): Either<ApiError, Conversation>
    suspend fun deleteSession(sessionId: String): Either<ApiError, Unit>
    suspend fun updateSessionTitle(sessionId: String, title: String): Either<ApiError, Unit>
}
