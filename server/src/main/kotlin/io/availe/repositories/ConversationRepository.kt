@file:OptIn(ExperimentalUuidApi::class)

package io.availe.repositories

import arrow.core.Option
import arrow.core.none
import arrow.core.some
import io.availe.jooq.enums.ConversationStatusTypeEnum
import io.availe.jooq.tables.references.CONVERSATIONS
import io.availe.models.*
import io.availe.repositories.utils.putIfSome
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
    fun fetchAllUserConversationIds(userAccountId: UserAccountId): Option<List<ConversationId>> {
        val records = dsl
            .select(CONVERSATIONS.ID)
            .from(CONVERSATIONS)
            .where(CONVERSATIONS.OWNER_ID.eq(userAccountId.value.toJavaUuid()))
            .fetch()

        if (records.isEmpty()) {
            return none()
        }

        return records.map { record ->
            ConversationId(checkNotNull(record.value1()).toKotlinUuid())
        }.some()
    }

    fun fetchConversationById(conversationId: ConversationId): Option<Conversation> {
        val record = dsl
            .selectFrom(CONVERSATIONS)
            .where(CONVERSATIONS.ID.eq(conversationId.value.toJavaUuid()))
            .fetchOne()

        if (record == null) {
            return none()
        }

        return Conversation(
            id = ConversationId(checkNotNull(record.id).toKotlinUuid()),
            title = ConversationTitle(record.title),
            createdAt = CreatedAt(checkNotNull(record.createdAt).toInstant().toKotlinInstant()),
            updatedAt = UpdatedAt(checkNotNull(record.updatedAt).toInstant().toKotlinInstant()),
            ownerId = UserAccountId(record.ownerId.toKotlinUuid()),
            status = record.status.toModel(),
            schemaVersion = ConversationSchemaVersion(record.schemaVersion)
        ).some()
    }

    fun streamAllUserConversations(userAccountId: UserAccountId): Flow<Conversation> = flow {
        val cursor = dsl
            .selectFrom(CONVERSATIONS)
            .where(CONVERSATIONS.OWNER_ID.eq(userAccountId.value.toJavaUuid()))
            .fetchSize(100)
            .fetchLazy()
        try {
            while (cursor.hasNext()) {
                val record = cursor.fetchNext()
                emit(
                    Conversation(
                        id = ConversationId(checkNotNull(checkNotNull(record).id).toKotlinUuid()),
                        title = ConversationTitle(record.title),
                        createdAt = CreatedAt(checkNotNull(record.createdAt).toInstant().toKotlinInstant()),
                        updatedAt = UpdatedAt(checkNotNull(record.updatedAt).toInstant().toKotlinInstant()),
                        ownerId = UserAccountId(record.ownerId.toKotlinUuid()),
                        status = record.status.toModel(),
                        schemaVersion = ConversationSchemaVersion(record.schemaVersion)
                    )
                )
            }
        } finally {
            cursor.close()
        }
    }.flowOn(Dispatchers.IO)

    fun insertConversation(create: ConversationCreate): Conversation {
        require(
            create.status == Conversation.ConversationStatus.ACTIVE || create.status == Conversation.ConversationStatus.TEMPORARY
        )

        val record = dsl
            .insertInto(CONVERSATIONS)
            .set(CONVERSATIONS.TITLE, create.title.value)
            .set(CONVERSATIONS.OWNER_ID, create.ownerId.value.toJavaUuid())
            .set(CONVERSATIONS.STATUS, create.status.toJooq())
            .set(CONVERSATIONS.SCHEMA_VERSION, create.schemaVersion.value)
            .returning(
                CONVERSATIONS.ID,
                CONVERSATIONS.TITLE,
                CONVERSATIONS.OWNER_ID,
                CONVERSATIONS.STATUS,
                CONVERSATIONS.SCHEMA_VERSION,
                CONVERSATIONS.CREATED_AT,
                CONVERSATIONS.UPDATED_AT
            )
            .fetchOne()
            ?: throw IllegalStateException("Failed to insert conversation")

        return Conversation(
            id = ConversationId(checkNotNull(record.id).toKotlinUuid()),
            title = ConversationTitle(record.title),
            createdAt = CreatedAt(checkNotNull(record.createdAt).toInstant().toKotlinInstant()),
            updatedAt = UpdatedAt(checkNotNull(record.updatedAt).toInstant().toKotlinInstant()),
            ownerId = UserAccountId(record.ownerId.toKotlinUuid()),
            status = record.status.toModel(),
            schemaVersion = ConversationSchemaVersion(record.schemaVersion)
        )
    }

    fun deleteConversationById(conversationId: ConversationId): Option<Unit> {
        val rowsDeleted: Int = dsl
            .deleteFrom(CONVERSATIONS)
            .where(CONVERSATIONS.ID.eq(conversationId.value.toJavaUuid()))
            .execute()

        return if (rowsDeleted > 0) Unit.some() else none()
    }

    fun patchConversation(conversationId: ConversationId, patch: ConversationPatch): Option<Unit> {
        val updates = mutableMapOf<Field<*>, Any>()

        updates.putIfSome(patch.title, CONVERSATIONS.TITLE) { it.value }
        updates.putIfSome(patch.status, CONVERSATIONS.STATUS) { it.toJooq() }

        if (updates.isEmpty()) return none()

        val rowsUpdated: Int = dsl
            .update(CONVERSATIONS)
            .set(updates)
            .where(CONVERSATIONS.ID.eq(conversationId.value.toJavaUuid()))
            .execute()

        return if (rowsUpdated > 0) Unit.some() else none()
    }
}

private fun Conversation.ConversationStatus.toJooq(): ConversationStatusTypeEnum =
    when (this) {
        Conversation.ConversationStatus.ACTIVE -> ConversationStatusTypeEnum.active
        Conversation.ConversationStatus.ARCHIVED -> ConversationStatusTypeEnum.archived
        Conversation.ConversationStatus.LOCAL -> ConversationStatusTypeEnum.local
        Conversation.ConversationStatus.TEMPORARY -> ConversationStatusTypeEnum.temporary
    }

private fun ConversationStatusTypeEnum.toModel(): Conversation.ConversationStatus =
    when (this) {
        ConversationStatusTypeEnum.active -> Conversation.ConversationStatus.ACTIVE
        ConversationStatusTypeEnum.archived -> Conversation.ConversationStatus.ARCHIVED
        ConversationStatusTypeEnum.local -> Conversation.ConversationStatus.LOCAL
        ConversationStatusTypeEnum.temporary -> Conversation.ConversationStatus.TEMPORARY
    }
