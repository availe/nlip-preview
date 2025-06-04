package io.availe.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.net.InetAddress
import java.time.Instant

@Serializable
@JvmInline
value class PasswordHash(val value: String)

@Serializable
@JvmInline
value class TwoFactorEnabled(val value: Boolean)

@Serializable
@JvmInline
value class TwoFactorSecret(val value: String)

@Serializable
@JvmInline
value class BanTimestamp(@Contextual val value: Instant)

@Serializable
@JvmInline
value class BanReason(val value: String)

@Serializable
@JvmInline
value class FailedLoginAttemptCount(val value: Int)

@Serializable
@JvmInline
value class LastFailedLoginTimestamp(@Contextual val value: Instant)

@Serializable
@JvmInline
value class AccountLockedUntilTimestamp(@Contextual val value: Instant)

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

@Serializable
@JvmInline
value class RegistrationIpAddress(@Contextual val value: InetAddress)

@Serializable
@JvmInline
value class LastLoginIpAddress(@Contextual val value: InetAddress)

@Serializable
@JvmInline
value class PreviousLoginIpAddresses(@Contextual val value: List<InetAddress>)

@Serializable
@JvmInline
value class DeviceToken(val value: String)

@Serializable
@JvmInline
value class KnownDeviceTokens(val value: List<DeviceToken>)

@Serializable
@JvmInline
value class LastModifiedTimestamp(@Contextual val value: Instant)

@Serializable
@JvmInline
value class UserAccountVersion(val value: Int)
