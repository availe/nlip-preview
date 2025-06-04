package io.availe.services.impl

import arrow.core.Either
import io.availe.services.ApiError
import io.availe.services.ApiErrorType
import io.availe.services.ChatStore
import io.availe.services.IChatService
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

object ChatServiceImpl : IChatService {
    override suspend fun getAllSessionIds(request: Unit): Either<ApiError, List<String>> =
        Either.catch {
            ChatStore.getAllSessionIdentifiers()
        }.mapLeft { e ->
            ApiError(
                type = ApiErrorType.SESSION_NOT_FOUND,
                message = e.message
            )
        }

    override val coroutineContext: CoroutineContext = Dispatchers.Default
}