@file:OptIn(ExperimentalUuidApi::class)

package io.availe.repositories

import arrow.core.Option
import arrow.core.none
import arrow.core.some
import io.availe.jooq.enums.ConversationStatusTypeEnum
import io.availe.jooq.tables.Conversations
import io.availe.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.datetime.toKotlinInstant
import org.jooq.DSLContext
import org.jooq.Field
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

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

    fun fetchConversationById(conversationId: ConversationId): Option<Conversation> {
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
            status = record.status.toModel(),
            version = ConversationSchemaVersion(record.schemaVersion)
        ).some()
    }

    fun streamAllUserConversations(userId: UserId): Flow<Conversation> = flow {
        val cursor = dsl
            .selectFrom(Conversations.CONVERSATIONS)
            .where(Conversations.CONVERSATIONS.OWNER_ID.eq(userId.id.toJavaUuid()))
            .fetchSize(100)
            .fetchLazy()
        try {
            while (cursor.hasNext()) {
                val record = cursor.fetchNext()
                emit(
                    Conversation(
                        id = ConversationId.from(checkNotNull(checkNotNull(record).id).toKotlinUuid()),
                        title = ConversationTitle(record.title),
                        createdAt = CreatedAt(checkNotNull(record.createdAt).toInstant().toKotlinInstant()),
                        updatedAt = UpdatedAt(checkNotNull(record.updatedAt).toInstant().toKotlinInstant()),
                        owner = UserId.from(record.ownerId.toKotlinUuid()),
                        status = record.status.toModel(),
                        version = ConversationSchemaVersion(record.schemaVersion)
                    )
                )
            }
        } finally {
            cursor.close()
        }
    }.flowOn(Dispatchers.IO)

    fun insertConversation(create: ConversationCreateRequest): Conversation {
        require(
            create.status == Conversation.Status.ACTIVE || create.status == Conversation.Status.TEMPORARY
        )

        val record = dsl
            .insertInto(Conversations.CONVERSATIONS)
            .set(Conversations.CONVERSATIONS.TITLE, create.title.title)
            .set(Conversations.CONVERSATIONS.OWNER_ID, create.owner.id.toJavaUuid())
            .set(Conversations.CONVERSATIONS.STATUS, create.status.toJooq())
            .set(Conversations.CONVERSATIONS.SCHEMA_VERSION, create.version.value)
            .returning(
                Conversations.CONVERSATIONS.ID,
                Conversations.CONVERSATIONS.TITLE,
                Conversations.CONVERSATIONS.OWNER_ID,
                Conversations.CONVERSATIONS.STATUS,
                Conversations.CONVERSATIONS.SCHEMA_VERSION,
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
            version = ConversationSchemaVersion(record.schemaVersion)
        )
    }

    fun deleteConversationById(conversationId: ConversationId): Option<Unit> {
        val rowsDeleted: Int = dsl
            .deleteFrom(Conversations.CONVERSATIONS)
            .where(Conversations.CONVERSATIONS.ID.eq(conversationId.id.toJavaUuid()))
            .execute()

        return if (rowsDeleted > 0) Unit.some() else none()
    }

    fun patchConversation(conversationId: ConversationId, update: ConversationPatchRequest): Option<Unit> {
        val updates = mutableMapOf<Field<*>, Any>()

        update.title?.let { updates[Conversations.CONVERSATIONS.TITLE] = it.title }
        update.status?.let { updates[Conversations.CONVERSATIONS.STATUS] = it.toJooq() }

        if (updates.isEmpty()) return none()

        val rowsUpdated: Int = dsl
            .update(Conversations.CONVERSATIONS)
            .set(updates)
            .where(Conversations.CONVERSATIONS.ID.eq(conversationId.id.toJavaUuid()))
            .execute()

        return if (rowsUpdated > 0) Unit.some() else none()
    }
}

private fun Conversation.Status.toJooq(): ConversationStatusTypeEnum =
    when (this) {
        Conversation.Status.ACTIVE -> ConversationStatusTypeEnum.active
        Conversation.Status.ARCHIVED -> ConversationStatusTypeEnum.archived
        Conversation.Status.LOCAL -> ConversationStatusTypeEnum.local
        Conversation.Status.TEMPORARY -> ConversationStatusTypeEnum.temporary
    }

private fun ConversationStatusTypeEnum.toModel(): Conversation.Status =
    when (this) {
        ConversationStatusTypeEnum.active -> Conversation.Status.ACTIVE
        ConversationStatusTypeEnum.archived -> Conversation.Status.ARCHIVED
        ConversationStatusTypeEnum.local -> Conversation.Status.LOCAL
        ConversationStatusTypeEnum.temporary -> Conversation.Status.TEMPORARY
    }
