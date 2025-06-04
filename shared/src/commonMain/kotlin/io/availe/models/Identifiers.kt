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

// Authentication and Identity
@Serializable
@JvmInline
value class Username(val value: String)

@Serializable
@JvmInline
value class EmailAddress(val value: String)

@Serializable
@JvmInline
value class PasswordHash(val value: String)

// Two-Factor Authentication
@Serializable
@JvmInline
value class TwoFactorEnabled(val value: Boolean)

@Serializable
@JvmInline
value class TwoFactorSecret(val value: String)

// Account Status and Ban
@Serializable
@JvmInline
value class AccountIsActive(val value: Boolean)

@Serializable
@JvmInline
value class BanTimestamp(@Contextual val value: Instant)

@Serializable
@JvmInline
value class BanReason(val value: String)

// Lockout Protection
@Serializable
@JvmInline
value class FailedLoginAttemptCount(val value: Int)

@Serializable
@JvmInline
value class LastFailedLoginTimestamp(@Contextual val value: Instant)

@Serializable
@JvmInline
value class AccountLockedUntilTimestamp(@Contextual val value: Instant)

// Audit and Presence
@Serializable
@JvmInline
value class AccountCreationTimestamp(@Contextual val value: Instant)

@Serializable
@JvmInline
value class LastPasswordChangeTimestamp(@Contextual val value: Instant)

@Serializable
@JvmInline
value class LastLoginTimestamp(@Contextual val value: Instant)

@Serializable
@JvmInline
value class LastSeenTimestamp(@Contextual val value: Instant)

// IP and Device Tracking
@Serializable
@JvmInline
value class RegistrationIpAddress(val value: String)

@Serializable
@JvmInline
value class LastLoginIpAddress(val value: String)

@Serializable
@JvmInline
value class PreviousLoginIpAddresses(val value: List<String>)

@Serializable
@JvmInline
value class KnownDeviceTokens(val value: List<String>)

// Role and Audit Metadata
@Serializable
@JvmInline
value class Role(val value: String)

@Serializable
@JvmInline
value class Roles(val value: List<Role>)

@Serializable
@JvmInline
value class LastModifiedTimestamp(@Contextual val value: Instant)