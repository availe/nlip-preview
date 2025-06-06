@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)

package io.availe.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
@JvmInline
value class MessageId private constructor(val id: Uuid) {
    companion object {
        fun from(uuid: Uuid): MessageId = MessageId(uuid)
    }
}

@Serializable
@JvmInline
value class ConversationId private constructor(val id: Uuid) {
    companion object {
        fun from(uuid: Uuid): ConversationId = ConversationId(uuid)
    }
}

@Serializable
@JvmInline
value class UserId private constructor(val id: Uuid) {
    companion object {
        fun from(uuid: Uuid): UserId = UserId(uuid)
    }
}

@Serializable
@JvmInline
value class AgentId private constructor(val id: Uuid) {
    companion object {
        fun from(uuid: Uuid): AgentId = AgentId(uuid)
    }
}

@Serializable
@JvmInline
value class SystemId private constructor(val id: Uuid) {
    companion object {
        fun from(uuid: Uuid): SystemId = SystemId(uuid)
    }
}

@Serializable
@JvmInline
value class CreatedAt(@Contextual val instant: Instant)

@Serializable
@JvmInline
value class UpdatedAt(@Contextual val instant: Instant)

@Serializable
@JvmInline
value class ConversationTitle(val title: String) {
    init {
        require(title.isNotBlank())
        require(title.length <= 100)
    }
}

@Serializable
@JvmInline
value class InternalMessageSchemaVersion(val value: Int) {
    init {
        require(value >= 1)
    }
}

@Serializable
@JvmInline
value class OutboundMessageSchemaVersion(val value: Int) {
    init {
        require(value >= 1)
    }
}

@Serializable
@JvmInline
value class ConversationSchemaVersion(val value: Int) {
    init {
        require(value >= 1)
    }
}

@Serializable
@JvmInline
value class UserAccountSchemaVersion(val value: Int) {
    init {
        require(value >= 1)
    }
}

@Serializable
sealed class Sender {
    @Serializable
    data class User(val id: UserId) : Sender() {
        companion object {
            fun fromId(uuid: Uuid) = User(UserId.from(uuid))
        }
    }

    @Serializable
    data class Agent(val id: AgentId) : Sender() {
        companion object {
            fun fromId(uuid: Uuid) = Agent(AgentId.from(uuid))
        }
    }

    @Serializable
    data class System(val id: SystemId) : Sender() {
        companion object {
            fun fromId(uuid: Uuid) = System(SystemId.from(uuid))
        }
    }
}

@Serializable
@JvmInline
value class Username(val value: String) {
    init {
        require(value.isNotBlank())
        require(value.length in 3..30)
        require(Regex("^[A-Za-z0-9._-]+$").matches(value))
    }
}

@Serializable
@JvmInline
value class EmailAddress(val value: String) {
    init {
        require(Regex("^[A-Za-z0-9+_.%-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(value))
    }
}

@Serializable
@JvmInline
value class AccountIsActive(val value: Boolean)