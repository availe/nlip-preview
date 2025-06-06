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
import org.jooq.Field
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid


sealed class ConversationError {
    data class UserNotFound(val id: UserId) : ConversationError()
    data class ConversationNotFound(val id: ConversationId) : ConversationError()
    data class CreationFailed(val id: ConversationId) : ConversationError()
}

class ConversationRepository(private val dsl: DSLContext) {
    fun fetchAllUserConversationIds(userId: UserId): Option<List<ConversationId>> {
        val records = dsl
            .select(Conversations.CONVERSATIONS.ID.asNonNullable())
            .from(Conversations.CONVERSATIONS)
            .where(Conversations.CONVERSATIONS.OWNER_ID.eq(userId.id.toJavaUuid()))
            .fetch()

        if (records.isEmpty()) {
            return none()
        }

        return records.map { record ->
            ConversationId.from(record.value1().toKotlinUuid())
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
            id = ConversationId.from(record.id.toKotlinUuid()),
            title = ConversationTitle(record.title),
            createdAt = CreatedAt(record.createdAt.toInstant().toKotlinInstant()),
            updatedAt = UpdatedAt(record.updatedAt.toInstant().toKotlinInstant()),
            owner = UserId.from(record.ownerId.toKotlinUuid()),
            status = Conversation.Status.valueOf(record.status.name),
            version = ConversationVersion(record.version)
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
            .set(Conversations.CONVERSATIONS.STATUS, ConversationStatusType.valueOf(create.status.name.lowercase()))
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
            id = ConversationId.from(record.id.toKotlinUuid()),
            title = ConversationTitle(record.title),
            createdAt = CreatedAt(record.createdAt.toInstant().toKotlinInstant()),
            updatedAt = UpdatedAt(record.updatedAt.toInstant().toKotlinInstant()),
            owner = UserId.from(record.ownerId.toKotlinUuid()),
            status = Conversation.Status.valueOf(record.status.name),
            version = ConversationVersion(record.version)
        )
    }

    fun deleteById(conversationId: ConversationId): Option<Unit> {
        val rowsDeleted = dsl
            .deleteFrom(Conversations.CONVERSATIONS)
            .where(Conversations.CONVERSATIONS.ID.eq(conversationId.id.toJavaUuid()))
            .execute()

        return if (rowsDeleted > 0) Unit.some() else none()
    }
}

/**
 * Convert a nullable Field<T?> into a non-nullable Field<T>.
 * At fetch time, if the database returns NULL for this column,
 * you'll get an IllegalStateException with the given message.
 */
fun <T> Field<T?>.asNonNullable(): Field<T> =
    this.convertFrom { value ->
        checkNotNull(value) { "Column [$this] was null but expected non-null" }
    }