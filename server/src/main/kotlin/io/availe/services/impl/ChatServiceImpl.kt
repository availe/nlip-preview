package io.availe.services.impl

import IChatService
import arrow.core.Either
import io.availe.models.Session
import io.availe.services.ApiError
import kotlin.coroutines.CoroutineContext

class ChatServiceImpl(override val coroutineContext: CoroutineContext) : IChatService {
    override suspend fun getAllSessions(request: Unit): Either<ApiError, List<Session>> {
        TODO("Not yet implemented")
    }
}