package io.availe.repositories

import arrow.core.Either
import io.availe.services.ApiError
import io.availe.services.IChatService
import kotlin.coroutines.CoroutineContext

class ChatRepository(
    private val chatService: IChatService,
    override val coroutineContext: CoroutineContext
) : IChatService {
    override suspend fun getAllSessionIds(request: Unit): Either<ApiError, List<String>> {
        return chatService.getAllSessionIds(request)
    }
}
