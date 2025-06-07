@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)

package io.availe.definitions

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.AnnotationSpec.UseSiteTarget
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import io.availe.core.codegen
import io.availe.core.generateEnum
import io.availe.core.generateValueClass
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonElement
import java.io.File
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun generateBaseTypes() {
    val out = File("../shared/build/generated-src/kotlin-poet/io/availe/models").apply { mkdirs() }
    val spec = codegen {
        valueClass("NlipMessageId", Long::class)
        valueClass("NlipSubmessageId", Long::class)
        valueClass("AttachmentId", Long::class)
        valueClass("InternalMessageId", Uuid::class)
        valueClass("ConversationId", Uuid::class)
        valueClass("UserAccountId", Uuid::class)

        valueClass("NlipMessageSchemaVersion", Int::class)
        valueClass("NlipSubmessageSchemaVersion", Int::class)
        valueClass("NlipMessageAttachmentSchemaVersion", Int::class)
        valueClass("NlipSubmessageAttachmentSchemaVersion", Int::class)
        valueClass("InternalMessageSchemaVersion", Int::class)
        valueClass("UserAccountSchemaVersion", Int::class)
        valueClass("InternalUserAccountSchemaVersion", Int::class)
        valueClass("ConversationSchemaVersion", Int::class)
        valueClass("ConnectionLocationAggregateSchemaVersion", Int::class)

        valueClass("CreatedAt", Instant::class)
        valueClass("UpdatedAt", Instant::class)
        valueClass("Username", String::class)
        valueClass("EmailAddress", String::class)
        valueClass("AccountIsActive", Boolean::class)

        valueClass("PasswordHash", String::class)
        valueClass("TwoFactorEnabled", Boolean::class)
        valueClass("TwoFactorSecret", String::class)
        valueClass("BanTimestamp", Instant::class)
        valueClass("BanReason", String::class)
        valueClass("FailedLoginAttemptCount", Int::class)
        valueClass("LastFailedLoginTimestamp", Instant::class)
        valueClass("AccountLockedUntilTimestamp", Instant::class)
        valueClass("AccountCreationTimestamp", Instant::class)
        valueClass("LastPasswordChangeTimestamp", Instant::class)
        valueClass("LastLoginTimestamp", Instant::class)
        valueClass("LastSeenTimestamp", Instant::class)
        valueClass("LastModifiedTimestamp", Instant::class)

        valueClass("Subformat", String::class)
        valueClass("ContentText", String::class)
        valueClass("ContentJson", JsonElement::class)
        valueClass("Label", String::class)
        valueClass("Position", Int::class)
        valueClass("FileKey", String::class)
        valueClass("ContentType", String::class)
        valueClass("FileSizeBytes", Long::class)
        valueClass("SenderId", Uuid::class)

        valueClass("ConversationTitle", String::class)

        valueClass("BucketDate", LocalDate::class)
        valueClass("CountryCode", String::class)
        valueClass("RegionCode", String::class)
        valueClass("ConnectionCount", Long::class)

        enum("AllowedFormatType", listOf("text", "token", "structured", "binary", "location", "error", "generic"))
        enum("MessageType", listOf("control"))
        enum("SenderType", listOf("user", "agent", "system"))
        enum("PlatformType", listOf("web", "ios", "android", "desktop"))
        enum("UserAccessType", listOf("anonymous", "authenticated"))
    }

    val optIn = ClassName("kotlin", "OptIn")
    val fileOptIn = AnnotationSpec.builder(optIn)
        .useSiteTarget(UseSiteTarget.FILE)
        .addMember("%T::class, %T::class", ExperimentalTime::class, ExperimentalUuidApi::class)
        .build()

    FileSpec.builder("io.availe.models", "Identifiers")
        .addAnnotation(fileOptIn)
        .apply {
            spec.wrappers.forEach { addType(generateValueClass(it)) }
            spec.enums.forEach { addType(generateEnum(it)) }
        }
        .build()
        .writeTo(out)
}
