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
data class ConversationId(val value: Uuid) {
    companion object Companion {
        fun from(uuid: Uuid) = ConversationId(uuid)
        fun generate() = ConversationId(Uuid.random())
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
sealed class Sender {
    abstract val id: Uuid
}

@Serializable
data class UserSender(val userId: UserId) : Sender() {
    override val id: Uuid get() = userId.value
}

@Serializable
data class AgentSender(val agentId: AgentId) : Sender() {
    override val id: Uuid get() = agentId.value
}

@Serializable
data class SystemSender(val systemId: SystemId) : Sender() {
    override val id: Uuid get() = systemId.value
}