@file:OptIn(ExperimentalUuidApi::class)

package io.availe.repositories

import arrow.core.*
import io.availe.jooq.enums.UserSubscriptionTierEnum
import io.availe.jooq.tables.records.UserAccountsRecord
import io.availe.jooq.tables.references.USER_ACCOUNTS
import io.availe.models.*
import io.availe.repositories.utils.nn
import io.availe.repositories.utils.putIfSome
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
            .selectFrom(USER_ACCOUNTS)
            .where(USER_ACCOUNTS.ID.eq(userAccountId.value.toJavaUuid()))
            .fetchOne()

        if (record == null) {
            return None
        }

        return record.toUserAccountModel().some()
    }

    internal fun insertUserAccount(userAccount: UserAccountCreate): Either<UserAccountError, UserAccount> {
        try {
            val record = dsl
                .insertInto(USER_ACCOUNTS)
                .set(USER_ACCOUNTS.USERNAME, userAccount.username.value)
                .set(USER_ACCOUNTS.EMAIL_ADDRESS, userAccount.emailAddress.value)
                .set(USER_ACCOUNTS.ACCOUNT_IS_ACTIVE, userAccount.accountIsActive.value)
                .set(USER_ACCOUNTS.SUBSCRIPTION_TIER, userAccount.subscriptionTier.toJooq())
                .set(USER_ACCOUNTS.SCHEMA_VERSION, userAccount.schemaVersion.value)
                .returning(
                    USER_ACCOUNTS.ID,
                    USER_ACCOUNTS.USERNAME,
                    USER_ACCOUNTS.EMAIL_ADDRESS,
                    USER_ACCOUNTS.ACCOUNT_IS_ACTIVE,
                    USER_ACCOUNTS.SUBSCRIPTION_TIER,
                    USER_ACCOUNTS.SCHEMA_VERSION,
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
            .deleteFrom(USER_ACCOUNTS)
            .where(USER_ACCOUNTS.ID.eq(userAccountId.value.toJavaUuid()))
            .execute()

        return if (rowsDeleted > 0) Unit.some() else None
    }

    internal fun patchUserAccount(userAccountId: UserAccountId, patch: UserAccountPatch): Option<Unit> {
        val updates = mutableMapOf<Field<*>, Any>()

        updates.putIfSome(patch.username, USER_ACCOUNTS.USERNAME) { it.value }
        updates.putIfSome(patch.emailAddress, USER_ACCOUNTS.EMAIL_ADDRESS) { it.value }
        updates.putIfSome(patch.accountIsActive, USER_ACCOUNTS.ACCOUNT_IS_ACTIVE) { it.value }
        updates.putIfSome(patch.subscriptionTier, USER_ACCOUNTS.SUBSCRIPTION_TIER) { it.toJooq() }

        if (updates.isEmpty()) return None

        val rowsUpdated: Int = dsl
            .update(USER_ACCOUNTS)
            .set(updates)
            .where(USER_ACCOUNTS.ID.eq(userAccountId.value.toJavaUuid()))
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

fun UserAccountsRecord.toUserAccountModel(): UserAccount =
    UserAccount(
        id = UserAccountId(nn(id, UserAccountsRecord::id).toKotlinUuid()),
        username = Username(nn(username, UserAccountsRecord::username)),
        emailAddress = EmailAddress(nn(emailAddress, UserAccountsRecord::emailAddress)),
        accountIsActive = AccountIsActive(nn(accountIsActive, UserAccountsRecord::accountIsActive)),
        subscriptionTier = nn(subscriptionTier, UserAccountsRecord::subscriptionTier).toModel(),
        schemaVersion = UserAccountSchemaVersion(nn(schemaVersion, UserAccountsRecord::schemaVersion))
    )