package io.availe.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class Session @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class) constructor(
    val id: Uuid,
    val title: String,
    @Contextual val createdAt: Instant,
    @Contextual val lastActivityAt: Instant,
    val participantIds: Set<Uuid>,
    val status: Status
) {
    @Serializable
    enum class Status { ACTIVE, ARCHIVED, LOCAL, TEMPORARY }
}
