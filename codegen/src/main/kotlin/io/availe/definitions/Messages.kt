@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)

package io.availe.definitions

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.AnnotationSpec.UseSiteTarget
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import io.availe.core.codegen
import io.availe.core.generateDataClass
import io.availe.core.generateEnum
import io.availe.core.generateValueClass
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonElement
import java.io.File
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun generateMessageModels() {
    val out = File("../shared/build/generated-src/kotlin-poet/io/availe/models").apply { mkdirs() }
    val spec = codegen {
        valueClass("NlipMessageId", Long::class)
        valueClass("NlipSubmessageId", Long::class)
        valueClass("AttachmentId", Long::class)
        valueClass("InternalMessageId", Uuid::class)
        valueClass("ConversationId", Uuid::class)
        valueClass("NlipMessageSchemaVersion", Int::class)
        valueClass("NlipSubmessageSchemaVersion", Int::class)
        valueClass("NlipMessageAttachmentSchemaVersion", Int::class)
        valueClass("NlipSubmessageAttachmentSchemaVersion", Int::class)
        valueClass("InternalMessageSchemaVersion", Int::class)

        enum("AllowedFormatType", listOf("text", "token", "structured", "binary", "location", "error", "generic"))
        enum("MessageType", listOf("control"))
        enum("SenderType", listOf("user", "agent", "system"))

        model("NlipMessage") {
            prop("id", "NlipMessageId")
            prop("format", "AllowedFormatType")
            prop("subformat", String::class)
            prop("contentText", String::class)
            prop("contentJson", JsonElement::class)
            prop("createdAt", Instant::class)
            prop("updatedAt", Instant::class)
            prop("schemaVersion", "NlipMessageSchemaVersion")
            prop("messageType", "MessageType")
            prop("label", String::class)
        }
        model("NlipSubmessage") {
            prop("id", "NlipSubmessageId")
            prop("nlipMessageId", "NlipMessageId")
            prop("position", Int::class)
            prop("format", "AllowedFormatType")
            prop("subformat", String::class)
            prop("contentText", String::class)
            prop("contentJson", JsonElement::class)
            prop("createdAt", Instant::class)
            prop("updatedAt", Instant::class)
            prop("schemaVersion", "NlipSubmessageSchemaVersion")
            prop("label", String::class)
        }
        model("NlipMessageAttachment") {
            prop("id", "AttachmentId")
            prop("nlipMessageId", "NlipMessageId")
            prop("fileKey", String::class)
            prop("contentType", String::class)
            prop("fileSizeBytes", Long::class)
            prop("createdAt", Instant::class)
            prop("schemaVersion", "NlipMessageAttachmentSchemaVersion")
        }
        model("NlipSubmessageAttachment") {
            prop("id", "AttachmentId")
            prop("nlipSubmessageId", "NlipSubmessageId")
            prop("fileKey", String::class)
            prop("contentType", String::class)
            prop("fileSizeBytes", Long::class)
            prop("createdAt", Instant::class)
            prop("schemaVersion", "NlipSubmessageAttachmentSchemaVersion")
        }
        model("InternalMessage") {
            prop("id", "InternalMessageId")
            prop("conversationId", "ConversationId")
            prop("senderType", "SenderType")
            prop("senderId", Uuid::class)
            prop("nlipMessageId", "NlipMessageId")
            prop("createdAt", Instant::class)
            prop("updatedAt", Instant::class)
            prop("parentMessageId", Uuid::class)
            prop("schemaVersion", "InternalMessageSchemaVersion")
        }
    }

    val pkg = "io.availe.models"
    val optIn = ClassName("kotlin", "OptIn")
    val fileOptIn = AnnotationSpec.builder(optIn)
        .useSiteTarget(UseSiteTarget.FILE)
        .addMember("%T::class, %T::class", ExperimentalTime::class, ExperimentalUuidApi::class)
        .build()

    FileSpec.builder(pkg, "Identifiers")
        .addAnnotation(fileOptIn)
        .apply {
            spec.wrappers.forEach { addType(generateValueClass(it)) }
            spec.enums.forEach { addType(generateEnum(it)) }
        }
        .build()
        .writeTo(out)

    spec.models.forEach { model ->
        FileSpec.builder(pkg, model.name)
            .addAnnotation(fileOptIn)
            .addType(generateDataClass(model))
            .build()
            .writeTo(out)
    }
}
