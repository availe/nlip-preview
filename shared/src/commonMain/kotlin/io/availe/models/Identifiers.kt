@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)

package io.availe.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
value class MessageId(val id: Uuid)

@Serializable
value class ConversationId(val id: Uuid)

@Serializable
value class UserId(val id: Uuid)

@Serializable
value class AgentId(val id: Uuid)

@Serializable
value class SystemId(val id: Uuid)

@Serializable
value class CreatedAt(@Contextual val instant: Instant)

@Serializable
value class UpdatedAt(@Contextual val instant: Instant)

@Serializable
value class ConversationTitle(val title: String)

@Serializable
value class InternalMessageVersion(val value: Int)

@Serializable
value class OutboundMessageVersion(val value: Int)

@Serializable
value class ConversationVersion(val value: Int)

@Serializable
value class UserAccountVersion(val value: Int)

@Serializable
sealed interface Sender {
    val uuid: Uuid
}

@Serializable
value class UserSender(val userId: UserId) : Sender {
    override val uuid: Uuid get() = userId.id
}

@Serializable
value class AgentSender(val agentId: AgentId) : Sender {
    override val uuid: Uuid get() = agentId.id
}

@Serializable
value class SystemSender(val systemId: SystemId) : Sender {
    override val uuid: Uuid get() = systemId.id
}