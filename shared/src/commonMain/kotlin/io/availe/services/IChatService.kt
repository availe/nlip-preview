package io.availe.services

import arrow.core.Either
import kotlinx.rpc.RemoteService
import kotlinx.rpc.annotations.Rpc

@Rpc
interface IChatService : RemoteService {
    suspend fun getAllSessionIds(request: Unit): Either<ApiError, List<String>>
}