@file:OptIn(ExperimentalUuidApi::class)

package io.availe.repositories

import arrow.core.Either
import arrow.core.Option
import arrow.core.none
import arrow.core.some
import io.availe.jooq.enums.UserRoleEnum
import io.availe.jooq.tables.references.INTERNAL_USER_ACCOUNTS
import io.availe.jooq.tables.references.USER_ACCOUNTS
import io.availe.models.*
import kotlinx.datetime.toKotlinInstant
import org.jooq.DSLContext
import java.time.Instant
import java.time.OffsetDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

sealed class InternalUserAccountError {
    object UserAlreadyExists : UserAccountError()
}

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

        val userAccountRecord = record.into(USER_ACCOUNTS)
        val internalRecord = record.into(INTERNAL_USER_ACCOUNTS)

        return InternalUserAccount(
            userAccount = userAccountRecord.toUserAccountModel(),
            passwordHash = PasswordHash(internalRecord.passwordHash),
            twoFactorEnabled = TwoFactorEnabled(internalRecord.twoFactorEnabled),
            twoFactorSecret = Option
                .fromNullable(internalRecord.twoFactorSecret)
                .map(::TwoFactorSecret),
            banTimestamp = Option
                .fromNullable(internalRecord.banTimestamp)
                .map(OffsetDateTime::toInstant)
                .map(Instant::toKotlinInstant)
                .map(::BanTimestamp),
            banReason = Option
                .fromNullable(internalRecord.banReason)
                .map(::BanReason),
            failedLoginAttemptCount = FailedLoginAttemptCount(internalRecord.failedLoginAttemptCount),
            lastFailedLoginTimestamp = Option
                .fromNullable(internalRecord.lastFailedLoginTimestamp)
                .map(OffsetDateTime::toInstant)
                .map(Instant::toKotlinInstant)
                .map(::LastFailedLoginTimestamp),
            accountLockedUntilTimestamp = Option
                .fromNullable(internalRecord.accountCreationTimestamp)
                .map(OffsetDateTime::toInstant)
                .map(Instant::toKotlinInstant)
                .map(::AccountLockedUntilTimestamp),
            accountCreationTimestamp = AccountCreationTimestamp(
                internalRecord.accountCreationTimestamp!!.toInstant().toKotlinInstant()
            ),
            lastPasswordChangeTimestamp = Option
                .fromNullable(internalRecord.lastPasswordChangeTimestamp)
                .map(OffsetDateTime::toInstant)
                .map(Instant::toKotlinInstant)
                .map(::LastPasswordChangeTimestamp),
            lastLoginTimestamp = Option
                .fromNullable(internalRecord.lastPasswordChangeTimestamp)
                .map(OffsetDateTime::toInstant)
                .map(Instant::toKotlinInstant)
                .map(::LastLoginTimestamp),
            lastSeenTimestamp = Option
                .fromNullable(internalRecord.lastSeenTimestamp)
                .map(OffsetDateTime::toInstant)
                .map(Instant::toKotlinInstant)
                .map(::LastSeenTimestamp),
            lastModifiedByUserId = Option
                .fromNullable(internalRecord.lastModifiedByUserId)
                .map { it.toKotlinUuid() }
                .map(::UserAccountId),
            lastModifiedTimestamp = Option
                .fromNullable(internalRecord.lastModifiedTimestamp)
                .map(OffsetDateTime::toInstant)
                .map(Instant::toKotlinInstant)
                .map(::LastModifiedTimestamp),
            userRole = internalRecord.userRole.toModel(),
            schemaVersion = InternalUserAccountSchemaVersion(internalRecord.schemaVersion)
        ).some()
    }

    fun insert(create: InternalUserAccountCreate): Either<InternalUserAccountError, InternalUserAccount> {
        val userAccount = userAccountRepository.insertUserAccount(create.userAccountCreate)

        val record = dsl
            .insertInto(INTERNAL_USER_ACCOUNTS)
            .set(INTERNAL_USER_ACCOUNTS.USER_ID, userAccount.getOrNull()!!.id.value.toJavaUuid())
            .set(INTERNAL_USER_ACCOUNTS.PASSWORD_HASH, create.passwordHash.value)
            .set(INTERNAL_USER_ACCOUNTS.TWO_FACTOR_ENABLED, create.twoFactorEnabled.value)
            .set(INTERNAL_USER_ACCOUNTS.TWO_FACTOR_SECRET, create.twoFactorSecret.getOrNull()?.value)
        TODO()
    }
}

private fun InternalUserAccount.UserRole.toJooq() {
    when (this) {
        InternalUserAccount.UserRole.FREE_USER -> UserRoleEnum.free_user
        InternalUserAccount.UserRole.PAID_USER -> UserRoleEnum.paid_user
        InternalUserAccount.UserRole.ADMIN -> UserRoleEnum.admin
    }
}

private fun UserRoleEnum.toModel(): InternalUserAccount.UserRole {
    return when (this) {
        UserRoleEnum.free_user -> InternalUserAccount.UserRole.FREE_USER
        UserRoleEnum.paid_user -> InternalUserAccount.UserRole.PAID_USER
        UserRoleEnum.admin -> InternalUserAccount.UserRole.ADMIN
    }
}