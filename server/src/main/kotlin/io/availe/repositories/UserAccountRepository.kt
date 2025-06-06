@file:OptIn(ExperimentalUuidApi::class)

package io.availe.repositories

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import io.availe.jooq.enums.ConversationStatusType
import io.availe.jooq.tables.UserAccounts
import io.availe.models.*
import org.jooq.DSLContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

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
        ).some()
    }
}

fun Conversation.Status.toJooq(): ConversationStatusType =
    when (this) {
        Conversation.Status.ACTIVE -> ConversationStatusType.active
        Conversation.Status.ARCHIVED -> ConversationStatusType.archived
        Conversation.Status.LOCAL -> ConversationStatusType.local
        Conversation.Status.TEMPORARY -> ConversationStatusType.temporary
    }

fun ConversationStatusType.toModel(): Conversation.Status =
    when (this) {
        ConversationStatusType.active -> Conversation.Status.ACTIVE
        ConversationStatusType.archived -> Conversation.Status.ARCHIVED
        ConversationStatusType.local -> Conversation.Status.LOCAL
        ConversationStatusType.temporary -> Conversation.Status.TEMPORARY
    }