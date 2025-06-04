@file:OptIn(ExperimentalTime::class)

package io.availe.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class Session(
    val id: SessionId,
    val title: String,
    @Contextual val createdAt: Instant,
    @Contextual val lastActivityAt: Instant,
    val participants: Participants,
    val status: Status
) {
    @Serializable
    enum class Status { ACTIVE, ARCHIVED, LOCAL, TEMPORARY }

    @Serializable
    data class Participants(
        val users: Set<UserSender>,
        val agents: Set<AgentSender>,
        val systems: Set<SystemSender>
    )
}