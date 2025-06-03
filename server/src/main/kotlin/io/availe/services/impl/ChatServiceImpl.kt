package io.availe.services.impl

import IChatService
import arrow.core.Either
import arrow.core.raise.either
import io.availe.models.Session
import io.availe.services.ApiError
import io.availe.services.ChatError
import io.availe.services.ChatStore
import io.availe.services.toApiError
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext


object ChatServiceImpl : IChatService {
    override suspend fun getAllSessions(request: Unit): Either<ApiError, List<Session>> = either {
        val sessionIds: List<String> = ChatStore.getAllSessionIdentifiers()

        sessionIds.map { sessionId ->
            ChatStore
                .getSession(sessionId)
                .mapLeft { chatError: ChatError -> chatError.toApiError() }
                .bind()
        }
    }

    override val coroutineContext: CoroutineContext = Dispatchers.Default
}