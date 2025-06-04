package io.availe.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class InternalUserAccount(
    val userAccount: UserAccount,
    val passwordHash: PasswordHash,
    val twoFactorEnabled: TwoFactorEnabled,
    val twoFactorSecret: TwoFactorSecret?,
    val banTimestamp: BanTimestamp?,
    val banReason: BanReason?,
    val failedLoginAttemptCount: FailedLoginAttemptCount,
    val lastFailedLoginTimestamp: LastFailedLoginTimestamp?,
    val accountLockedUntilTimestamp: AccountLockedUntilTimestamp?,
    @Contextual val accountCreationTimestamp: AccountCreationTimestamp,
    @Contextual val lastPasswordChangeTimestamp: LastPasswordChangeTimestamp?,
    @Contextual val lastLoginTimestamp: LastLoginTimestamp?,
    @Contextual val lastSeenTimestamp: LastSeenTimestamp?,
    val registrationIpAddress: RegistrationIpAddress,
    val lastLoginIpAddress: LastLoginIpAddress?,
    val previousLoginIpAddresses: PreviousLoginIpAddresses,
    val knownDeviceTokens: KnownDeviceTokens,
    val lastModifiedByUserId: UserId?,
    @Contextual val lastModifiedTimestamp: LastModifiedTimestamp?,
    val userAccountVersion: UserAccountVersion
)
