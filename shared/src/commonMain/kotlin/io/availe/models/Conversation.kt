@file:OptIn(ExperimentalUuidApi::class)

package io.availe.models

import io.availe.ModelGen
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private interface Conversation

@ModelGen(
    replication = Replication.BOTH,
    annotations = [Serializable::class],
    optInMarkers = [ExperimentalUuidApi::class]
)
private interface V1 : Conversation {
    val id: Uuid
    val title: String
    val createdAt: Instant
    val updatedAt: Instant
    val ownerId: Uuid
    val status: ConversationStatus
}

enum class ConversationStatus {
    ACTIVE,
    ARCHIVED,
    DELETED,
}