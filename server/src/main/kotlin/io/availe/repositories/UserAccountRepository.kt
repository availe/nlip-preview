@file:OptIn(ExperimentalUuidApi::class)

package io.availe.repositories

import arrow.core.*
import io.availe.jooq.enums.UserSubscriptionTierEnum
import io.availe.jooq.tables.UserAccounts
import io.availe.models.*
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.exception.DataAccessException
import org.postgresql.util.PSQLState
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

sealed class UserAccountError {
    object UserAlreadyExists : UserAccountError()
}

class UserAccountRepository(private val dsl: DSLContext) {
    fun fetchById(userAccountId: UserAccountId): Option<UserAccount> {
        val record = dsl
            .selectFrom(UserAccounts.USER_ACCOUNTS)
            .where(UserAccounts.USER_ACCOUNTS.ID.eq(userAccountId.value.toJavaUuid()))
            .fetchOne()

        if (record == null) {
            return None
        }

        return UserAccount(
            id = UserAccountId(checkNotNull(record.id).toKotlinUuid()),
            username = Username(record.username),
            emailAddress = EmailAddress(record.emailAddress),
            accountIsActive = AccountIsActive(record.accountIsActive),
            subscriptionTier = record.subscriptionTier.toModel(),
            schemaVersion = UserAccountSchemaVersion(record.schemaVersion),
        ).some()
    }

    internal fun insertUserAccount(userAccount: UserAccountCreate): Either<UserAccountError, UserAccount> {
        try {
            val record = dsl
                .insertInto(UserAccounts.USER_ACCOUNTS)
                .set(UserAccounts.USER_ACCOUNTS.USERNAME, userAccount.username.value)
                .set(UserAccounts.USER_ACCOUNTS.EMAIL_ADDRESS, userAccount.emailAddress.value)
                .set(UserAccounts.USER_ACCOUNTS.ACCOUNT_IS_ACTIVE, userAccount.accountIsActive.value)
                .set(UserAccounts.USER_ACCOUNTS.SUBSCRIPTION_TIER, userAccount.subscriptionTier.toJooq())
                .set(UserAccounts.USER_ACCOUNTS.SCHEMA_VERSION, userAccount.schemaVersion.value)
                .returning(
                    UserAccounts.USER_ACCOUNTS.ID,
                    UserAccounts.USER_ACCOUNTS.USERNAME,
                    UserAccounts.USER_ACCOUNTS.EMAIL_ADDRESS,
                    UserAccounts.USER_ACCOUNTS.ACCOUNT_IS_ACTIVE,
                    UserAccounts.USER_ACCOUNTS.SUBSCRIPTION_TIER,
                    UserAccounts.USER_ACCOUNTS.SCHEMA_VERSION,
                ).fetchOne()

            return UserAccount(
                id = UserAccountId(checkNotNull(checkNotNull(record).id).toKotlinUuid()),
                username = Username(checkNotNull(record).username),
                emailAddress = EmailAddress(checkNotNull(record).emailAddress),
                accountIsActive = AccountIsActive(record.accountIsActive),
                subscriptionTier = record.subscriptionTier.toModel(),
                schemaVersion = UserAccountSchemaVersion(record.schemaVersion),
            ).right()
        } catch (e: DataAccessException) {
            if (e.sqlState() == PSQLState.UNIQUE_VIOLATION.state) {
                return UserAccountError.UserAlreadyExists.left()
            }
            throw e
        }
    }

    internal fun deleteUserAccount(userAccountId: UserAccountId): Option<Unit> {
        val rowsDeleted: Int = dsl
            .deleteFrom(UserAccounts.USER_ACCOUNTS)
            .where(UserAccounts.USER_ACCOUNTS.ID.eq(userAccountId.value.toJavaUuid()))
            .execute()

        return if (rowsDeleted > 0) Unit.some() else None
    }

    internal fun patchUserAccount(userAccountId: UserAccountId, patch: UserAccountPatch): Option<Unit> {
        val updates = mutableMapOf<Field<*>, Any>()

        patch.username.getOrNull()
            ?.let { newUsername -> updates[UserAccounts.USER_ACCOUNTS.USERNAME] = newUsername.value }
        patch.accountIsActive.getOrNull()?.let { updates[UserAccounts.USER_ACCOUNTS.ACCOUNT_IS_ACTIVE] = it.value }
        patch.emailAddress.getOrNull()?.let { updates[UserAccounts.USER_ACCOUNTS.EMAIL_ADDRESS] = it.value }
        patch.subscriptionTier.getOrNull()?.let { updates[UserAccounts.USER_ACCOUNTS.SUBSCRIPTION_TIER] = it.toJooq() }

        if (updates.isEmpty()) return None

        val rowsUpdated: Int = dsl
            .update(UserAccounts.USER_ACCOUNTS)
            .set(updates)
            .where(UserAccounts.USER_ACCOUNTS.ID.eq(userAccountId.value.toJavaUuid()))
            .execute()

        return if (rowsUpdated > 0) Unit.some() else None
    }
}

private fun UserAccount.UserSubscriptionTier.toJooq() =
    when (this) {
        UserAccount.UserSubscriptionTier.STANDARD -> UserSubscriptionTierEnum.standard
        UserAccount.UserSubscriptionTier.BYOK -> UserSubscriptionTierEnum.byok
        UserAccount.UserSubscriptionTier.ENTERPRISE -> UserSubscriptionTierEnum.enterprise
    }

private fun UserSubscriptionTierEnum.toModel() =
    when (this) {
        UserSubscriptionTierEnum.standard -> UserAccount.UserSubscriptionTier.STANDARD
        UserSubscriptionTierEnum.byok -> UserAccount.UserSubscriptionTier.BYOK
        UserSubscriptionTierEnum.enterprise -> UserAccount.UserSubscriptionTier.ENTERPRISE
    }
