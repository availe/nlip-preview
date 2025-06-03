package io.availe.services.impl

import IChatService
import arrow.core.Either
import io.availe.models.Session
import io.availe.services.ApiError
import io.availe.services.ChatStore
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

object ChatServiceImpl : IChatService {
    override suspend fun getAllSessions(request: Unit): Either<ApiError, List<Session>> {
        val id = ChatStore.getAllSessionIdentifiers()

        TODO("Not yet implemented")
    }

    override val coroutineContext: CoroutineContext = Dispatchers.Default
}