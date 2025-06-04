@file:OptIn(ExperimentalTime::class)

package io.availe.models

import io.availe.openapi.model.NLIPRequest
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class InternalMessage(
    val id: MessageId,
    val sessionId: SessionId,
    val sender: Sender,
    val senderRole: Role,
    val nlipMessage: NLIPRequest,
    @Contextual val timeStamp: Instant,
    val status: Status,
    val parentMessageId: MessageId?
) {
    @Serializable
    enum class Role { USER, AGENT, SYSTEM, AUDITOR, OTHER }

    @Serializable
    enum class Status { PENDING, SENT, RECEIVED, ERROR }
}
