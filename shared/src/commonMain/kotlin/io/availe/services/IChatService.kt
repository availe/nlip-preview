import arrow.core.Either
import io.availe.models.Session
import io.availe.services.ApiError
import kotlinx.rpc.RemoteService
import kotlinx.rpc.annotations.Rpc

@Rpc
interface IChatService : RemoteService {
    suspend fun getAllSessions(request: Unit): Either<ApiError, List<Session>>
}