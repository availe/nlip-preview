package io.availe.models

import io.availe.openapi.model.NLIPRequest
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class InternalMessage @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class) constructor(
    val id: Uuid,
    val sessionId: Uuid,
    val senderId: Uuid,
    val senderRole: Role,
    val nlipMessage: NLIPRequest,
    @Contextual val timeStamp: Instant,
    val status: Status,
    val parentMessageId: Uuid?
) {
    @Serializable
    enum class Role { USER, AGENT, SYSTEM, AUDITOR, OTHER }

    @Serializable
    enum class Status { PENDING, SENT, RECEIVED, ERROR }
}
