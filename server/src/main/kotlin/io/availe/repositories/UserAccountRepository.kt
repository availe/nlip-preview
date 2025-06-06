@file:OptIn(ExperimentalUuidApi::class)

package io.availe.repositories

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.raise.either
import arrow.core.some
import io.availe.jooq.enums.UserSubscriptionTierEnum
import io.availe.jooq.tables.UserAccounts
import io.availe.models.*
import org.jooq.DSLContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

sealed class UserAccountError {
    object UserAlreadyExists : UserAccountError()
}

class UserAccountRepository(private val dsl: DSLContext) {
    fun fetchById(userId: UserId): Option<UserAccount> {
        val record = dsl
            .selectFrom(UserAccounts.USER_ACCOUNTS)
            .where(UserAccounts.USER_ACCOUNTS.ID.eq(userId.id.toJavaUuid()))
            .fetchOne()

        if (record == null) {
            return None
        }

        return UserAccount(
            userId = UserId.from(checkNotNull(record.id).toKotlinUuid()),
            username = Username(record.username),
            emailAddress = EmailAddress(record.emailAddress),
            accountIsActive = AccountIsActive(record.accountIsActive),
            userSubscriptionTier = record.userSubscriptionTier.toModel(),
            schemaVersion = UserAccountSchemaVersion(record.userAccountSchemaVersion),
        ).some()
    }

    internal fun insert(user: UserAccount): Either<UserAccountError, UserAccount> = either {
        TODO()
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
