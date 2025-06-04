package io.availe.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class UserAccount(
    // Primary Identifier
    val userId: UserId,

    // Authentication and Identity
    val username: Username,
    val emailAddress: EmailAddress,
    val passwordHash: PasswordHash,

    // Two-Factor Authentication
    val twoFactorEnabled: TwoFactorEnabled,
    val twoFactorSecret: TwoFactorSecret?,

    // Account Status and Ban
    val accountIsActive: AccountIsActive,
    val banTimestamp: BanTimestamp?,
    val banReason: BanReason?,

    // Lockout Protection
    val failedLoginAttemptCount: FailedLoginAttemptCount,
    val lastFailedLoginTimestamp: LastFailedLoginTimestamp?,
    val accountLockedUntilTimestamp: AccountLockedUntilTimestamp?,

    // Audit and Presence
    @Contextual
    val accountCreationTimestamp: AccountCreationTimestamp,
    @Contextual
    val lastPasswordChangeTimestamp: LastPasswordChangeTimestamp?,
    @Contextual
    val lastLoginTimestamp: LastLoginTimestamp?,
    @Contextual
    val lastSeenTimestamp: LastSeenTimestamp?,

    // IP and Device Tracking
    val registrationIpAddress: RegistrationIpAddress,
    val lastLoginIpAddress: LastLoginIpAddress?,
    val previousLoginIpAddresses: PreviousLoginIpAddresses,
    val knownDeviceTokens: KnownDeviceTokens,

    // Role and Audit Metadata
    val roles: Roles,
    val lastModifiedByUserId: UserId?,
    @Contextual
    val lastModifiedTimestamp: LastModifiedTimestamp?,

    // Versioning
    val userAccountVersion: UserAccountVersion
)
