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
    val version: ConversationVersion
) {
    @Serializable
    enum class Status { ACTIVE, ARCHIVED, LOCAL, TEMPORARY }
}

@Serializable
data class ConversationCreate(
    val title: ConversationTitle,
    val owner: UserId,
    val status: Conversation.Status,
    val version: ConversationVersion
)