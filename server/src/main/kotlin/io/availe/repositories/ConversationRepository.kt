@file:OptIn(ExperimentalUuidApi::class)

package io.availe.repositories

import arrow.core.Option
import arrow.core.none
import arrow.core.some
import io.availe.jooq.enums.ConversationStatusType
import io.availe.jooq.tables.Conversations
import io.availe.models.*
import kotlinx.datetime.toKotlinInstant
import org.jooq.DSLContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

sealed class ConversationUpdate {
    data class ChangeTitle(val newTitle: String) : ConversationUpdate()
    data class ChangeStatus(val newStatus: Conversation.Status) : ConversationUpdate()
}

class ConversationRepository(private val dsl: DSLContext) {
    fun fetchAllUserConversationIds(userId: UserId): Option<List<ConversationId>> {
        val records = dsl
            .select(Conversations.CONVERSATIONS.ID)
            .from(Conversations.CONVERSATIONS)
            .where(Conversations.CONVERSATIONS.OWNER_ID.eq(userId.id.toJavaUuid()))
            .fetch()

        if (records.isEmpty()) {
            return none()
        }

        return records.map { record ->
            ConversationId.from(checkNotNull(record.value1()).toKotlinUuid())
        }.some()
    }

    fun fetchById(conversationId: ConversationId): Option<Conversation> {
        val record = dsl
            .selectFrom(Conversations.CONVERSATIONS)
            .where(Conversations.CONVERSATIONS.ID.eq(conversationId.id.toJavaUuid()))
            .fetchOne()

        if (record == null) {
            return none()
        }

        return Conversation(
            id = ConversationId.from(checkNotNull(record.id).toKotlinUuid()),
            title = ConversationTitle(record.title),
            createdAt = CreatedAt(checkNotNull(record.createdAt).toInstant().toKotlinInstant()),
            updatedAt = UpdatedAt(checkNotNull(record.updatedAt).toInstant().toKotlinInstant()),
            owner = UserId.from(record.ownerId.toKotlinUuid()),
            status = Conversation.Status.valueOf(record.status.name),
            version = ConversationSchemaVersion(record.version)
        ).some()
    }

    fun insert(create: ConversationCreate): Conversation {
        require(
            create.status == Conversation.Status.ACTIVE || create.status == Conversation.Status.TEMPORARY
        )

        val record = dsl
            .insertInto(Conversations.CONVERSATIONS)
            .set(Conversations.CONVERSATIONS.TITLE, create.title.title)
            .set(Conversations.CONVERSATIONS.OWNER_ID, create.owner.id.toJavaUuid())
            .set(Conversations.CONVERSATIONS.STATUS, create.status.toJooq())
            .set(Conversations.CONVERSATIONS.VERSION, create.version.value)
            .returning(
                Conversations.CONVERSATIONS.ID,
                Conversations.CONVERSATIONS.TITLE,
                Conversations.CONVERSATIONS.OWNER_ID,
                Conversations.CONVERSATIONS.STATUS,
                Conversations.CONVERSATIONS.VERSION,
                Conversations.CONVERSATIONS.CREATED_AT,
                Conversations.CONVERSATIONS.UPDATED_AT
            )
            .fetchOne()
            ?: throw IllegalStateException("Failed to insert conversation")

        return Conversation(
            id = ConversationId.from(checkNotNull(record.id).toKotlinUuid()),
            title = ConversationTitle(record.title),
            createdAt = CreatedAt(checkNotNull(record.createdAt).toInstant().toKotlinInstant()),
            updatedAt = UpdatedAt(checkNotNull(record.updatedAt).toInstant().toKotlinInstant()),
            owner = UserId.from(record.ownerId.toKotlinUuid()),
            status = record.status.toModel(),
            version = ConversationSchemaVersion(record.version)
        )
    }

    fun deleteById(conversationId: ConversationId): Option<Unit> {
        val rowsDeleted = dsl
            .deleteFrom(Conversations.CONVERSATIONS)
            .where(Conversations.CONVERSATIONS.ID.eq(conversationId.id.toJavaUuid()))
            .execute()

        return if (rowsDeleted > 0) Unit.some() else none()
    }

    fun applyUpdate(conversationId: ConversationId, update: ConversationUpdate): Option<Conversation> {}
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
