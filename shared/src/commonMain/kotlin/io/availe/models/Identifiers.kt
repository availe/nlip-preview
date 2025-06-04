@file:OptIn(ExperimentalUuidApi::class)

package io.availe.models

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class MessageId(val value: Uuid) {
    companion object {
        fun from(uuid: Uuid) = MessageId(uuid)
        fun generate() = MessageId(Uuid.random())
    }
}

@Serializable
data class SessionId(val value: Uuid) {
    companion object {
        fun from(uuid: Uuid) = SessionId(uuid)
        fun generate() = SessionId(Uuid.random())
    }
}

@Serializable
data class UserId(val value: Uuid) {
    companion object {
        fun from(uuid: Uuid) = UserId(uuid)
        fun generate() = UserId(Uuid.random())
    }
}

@Serializable
data class AgentId(val value: Uuid) {
    companion object {
        fun from(uuid: Uuid) = AgentId(uuid)
        fun generate() = AgentId(Uuid.random())
    }
}

@Serializable
data class SystemId(val value: Uuid) {
    companion object {
        fun from(uuid: Uuid) = SystemId(uuid)
        fun generate() = SystemId(Uuid.random())
    }
}

@Serializable
sealed interface Sender

@Serializable
data class UserSender(val id: UserId) : Sender

@Serializable
data class AgentSender(val id: AgentId) : Sender

@Serializable
data class SystemSender(val id: SystemId) : Sender
