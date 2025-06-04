@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)

package io.availe.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
@JvmInline
value class MessageId(val id: Uuid)

@Serializable
@JvmInline
value class ConversationId(val id: Uuid)

@Serializable
@JvmInline
value class UserId(val id: Uuid)

@Serializable
@JvmInline
value class AgentId(val id: Uuid)

@Serializable
@JvmInline
value class SystemId(val id: Uuid)

@Serializable
@JvmInline
value class CreatedAt(@Contextual val instant: Instant)

@Serializable
@JvmInline
value class UpdatedAt(@Contextual val instant: Instant)

@Serializable
@JvmInline
value class ConversationTitle(val title: String)

@Serializable
@JvmInline
value class InternalMessageVersion(val value: Int)

@Serializable
@JvmInline
value class OutboundMessageVersion(val value: Int)

@Serializable
@JvmInline
value class ConversationVersion(val value: Int)

@Serializable
@JvmInline
value class UserAccountVersion(val value: Int)

@Serializable
sealed class Sender {
    @Serializable
    data class User(val id: UserId) : Sender() {
        companion object {
            fun fromId(uuid: Uuid) = User(UserId(uuid))
        }
    }

    @Serializable
    data class Agent(val id: AgentId) : Sender() {
        companion object {
            fun fromId(uuid: Uuid) = Agent(AgentId(uuid))
        }
    }

    @Serializable
    data class System(val id: SystemId) : Sender() {
        companion object {
            fun fromId(uuid: Uuid) = System(SystemId(uuid))
        }
    }
}

//** ------------- User value types ------------- */

@Serializable
@JvmInline
value class Username(val value: String)

@Serializable
@JvmInline
value class EmailAddress(val value: String)

@Serializable
@JvmInline
value class AccountIsActive(val value: Boolean)

@Serializable
@JvmInline
value class Role(val value: String)

@Serializable
@JvmInline
value class Roles(val value: List<Role>)