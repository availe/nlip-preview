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
data class ParticipantId(val value: Uuid) {
    companion object {
        fun from(uuid: Uuid) = ParticipantId(uuid)
        fun generate() = ParticipantId(Uuid.random())
    }
}

@Serializable
sealed interface Sender {
    val id: ParticipantId
}

@Serializable
data class UserSender(override val id: ParticipantId) : Sender

@Serializable
data class AgentSender(override val id: ParticipantId) : Sender

@Serializable
data class SystemSender(override val id: ParticipantId) : Sender

@Serializable
data class AuditorSender(override val id: ParticipantId) : Sender

@Serializable
data class OtherSender(override val id: ParticipantId) : Sender
