@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)

package io.availe.definitions

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.AnnotationSpec.UseSiteTarget
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asTypeName
import io.availe.core.codegen
import io.availe.core.generateEnum
import io.availe.core.generateValueClass
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonElement
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun generateBaseTypes() {
    val listOfString = ClassName("kotlin.collections", "List").parameterizedBy(String::class.asTypeName())
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
        valueClass("BanTimestamp", Instant::class)
        valueClass("LastFailedLoginTimestamp", Instant::class)
        valueClass("AccountLockedUntilTimestamp", Instant::class)
        valueClass("AccountCreationTimestamp", Instant::class)
        valueClass("LastPasswordChangeTimestamp", Instant::class)
        valueClass("LastLoginTimestamp", Instant::class)
        valueClass("LastSeenTimestamp", Instant::class)
        valueClass("LastModifiedTimestamp", Instant::class)
        valueClass("BucketDate", LocalDate::class)
        valueClass("Username", String::class)
        valueClass("EmailAddress", String::class)
        valueClass("AccountIsActive", Boolean::class)
        valueClass("PasswordHash", String::class)
        valueClass("TwoFactorEnabled", Boolean::class)
        valueClass("TwoFactorSecret", String::class)
        valueClass("BanReason", String::class)
        valueClass("FailedLoginAttemptCount", Int::class)
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
        valueClass("CountryCode", String::class)
        valueClass("RegionCode", String::class)
        valueClass("ConnectionCount", Long::class)
        valueClass("RegistrationIpAddress", String::class)
        valueClass("LastLoginIpAddress", String::class)
        valueClass("PreviousLoginIpAddresses", listOfString)
        valueClass("KnownDeviceTokens", listOfString)
        enum("AllowedFormatType", listOf("text", "token", "structured", "binary", "location", "error", "generic"))
        enum("MessageType", listOf("control"))
        enum("SenderType", listOf("user", "agent", "system"))
        enum("PlatformType", listOf("web", "ios", "android", "desktop"))
        enum("UserAccessType", listOf("anonymous", "authenticated"))
    }
    val fileOptIn = AnnotationSpec.builder(ClassName("kotlin", "OptIn"))
        .useSiteTarget(UseSiteTarget.FILE)
        .addMember("%T::class, %T::class", ExperimentalTime::class, ExperimentalUuidApi::class)
        .build()

    fun writeIdentifiersTo(root: java.io.File) {
        FileSpec.builder("io.availe.models", "Identifiers")
            .addAnnotation(fileOptIn)
            .apply {
                spec.wrappers.forEach { addType(generateValueClass(it)) }
                spec.enums.forEach { addType(generateEnum(it)) }
            }
            .build()
            .writeTo(root)
    }
    writeIdentifiersTo(Paths.sharedRoot)
    writeIdentifiersTo(Paths.serverRoot)
}
