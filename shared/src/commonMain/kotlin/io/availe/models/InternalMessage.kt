@file:OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)

package io.availe.models

import io.availe.openapi.model.NLIPRequest
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi


@Serializable
data class InternalMessage(
    val id: MessageId,
    val conversationId: ConversationId,
    val sender: Sender,
    val nlipMessage: NLIPRequest,
    val createdAt: CreatedAt,
    val updatedAt: UpdatedAt,
    val parentMessageId: MessageId?,
    val version: InternalMessageVersion
)