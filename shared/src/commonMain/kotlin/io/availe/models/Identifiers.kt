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
sealed class Sender {
    @Serializable
    data class User(val id: UserId) : Sender() {
        companion object {
            fun create(uuid: Uuid) = User(UserId(uuid))
        }
    }

    @Serializable
    data class Agent(val id: AgentId) : Sender() {
        companion object {
            fun create(uuid: Uuid) = Agent(AgentId(uuid))
        }
    }

    @Serializable
    data class System(val id: SystemId) : Sender() {
        companion object {
            fun create(uuid: Uuid) = System(SystemId(uuid))
        }
    }
}
