@file:OptIn(ExperimentalUuidApi::class)

package io.availe.repositories

import arrow.core.Option
import arrow.core.none
import arrow.core.some
import io.availe.jooq.tables.references.INTERNAL_USER_ACCOUNTS
import io.availe.jooq.tables.references.USER_ACCOUNTS
import io.availe.models.InternalUserAccount
import io.availe.models.UserAccountId
import org.jooq.DSLContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toJavaUuid

class InternalUserAccountRepository(
    private val dsl: DSLContext,
    private val userAccountRepository: UserAccountRepository
) {
    fun findById(userAccountId: UserAccountId): Option<InternalUserAccount> {
        val record = dsl
            .select(INTERNAL_USER_ACCOUNTS.asterisk(), USER_ACCOUNTS.asterisk())
            .from(INTERNAL_USER_ACCOUNTS)
            .join(USER_ACCOUNTS).on(INTERNAL_USER_ACCOUNTS.USER_ID.eq(USER_ACCOUNTS.ID))
            .where(INTERNAL_USER_ACCOUNTS.USER_ID.eq(userAccountId.value.toJavaUuid()))
            .fetchOne()
        
        if (record == null) {
            return none()
        }

        return InternalUserAccount(
            userAccount = TODO(),
            passwordHash = TODO(),
            twoFactorEnabled = TODO(),
            twoFactorSecret = TODO(),
            banTimestamp = TODO(),
            banReason = TODO(),
            failedLoginAttemptCount = TODO(),
            lastFailedLoginTimestamp = TODO(),
            accountLockedUntilTimestamp = TODO(),
            accountCreationTimestamp = TODO(),
            lastPasswordChangeTimestamp = TODO(),
            lastLoginTimestamp = TODO(),
            lastSeenTimestamp = TODO(),
            registrationIpAddress = TODO(),
            lastLoginIpAddress = TODO(),
            previousLoginIpAddresses = TODO(),
            knownDeviceTokens = TODO(),
            lastModifiedByUserId = TODO(),
            lastModifiedTimestamp = TODO(),
            userRole = TODO(),
            schemaVersion = TODO()
        ).some()
    }
}