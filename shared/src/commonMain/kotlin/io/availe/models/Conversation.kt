@file:OptIn(ExperimentalTime::class)

package io.availe.models

import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime

@Serializable
data class Conversation(
    val id: ConversationId,
    val title: ConversationTitle,
    val createdAt: CreatedAt,
    val updatedAt: UpdatedAt,
    val owner: UserId,
    val status: Status,
    val version: ConversationSchemaVersion
) {
    @Serializable
    enum class Status { ACTIVE, ARCHIVED, LOCAL, TEMPORARY }
}

@Serializable
data class ConversationCreateRequest(
    val title: ConversationTitle,
    val owner: UserId,
    val status: Conversation.Status,
    val version: ConversationSchemaVersion
)

data class ConversationPatchRequest(
    val title: ConversationTitle? = null,
    val status: Conversation.Status? = null,
)
