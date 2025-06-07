@file:OptIn(ExperimentalUuidApi::class)

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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import java.io.File
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun generateMessageModels() {
    val out = File("../shared/build/generated-src/kotlin-poet/io/availe/models").also { it.mkdirs() }
    val spec = codegen {
        inlineValue("NlipMessageId", Long::class)
        inlineValue("NlipSubmessageId", Long::class)
        inlineValue("AttachmentId", Long::class)
        inlineValue("InternalMessageId", Uuid::class)
        inlineValue("ConversationId", Uuid::class)
        inlineValue("SchemaVersion", Int::class)

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
            prop("schemaVersion", "SchemaVersion")
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
            prop("schemaVersion", "SchemaVersion")
            prop("label", String::class)
        }

        model("NlipMessageAttachment") {
            prop("id", "AttachmentId")
            prop("nlipMessageId", "NlipMessageId")
            prop("fileKey", String::class)
            prop("contentType", String::class)
            prop("fileSizeBytes", Long::class)
            prop("createdAt", Instant::class)
            prop("schemaVersion", "SchemaVersion")
        }

        model("NlipSubmessageAttachment") {
            prop("id", "AttachmentId")
            prop("nlipSubmessageId", "NlipSubmessageId")
            prop("fileKey", String::class)
            prop("contentType", String::class)
            prop("fileSizeBytes", Long::class)
            prop("createdAt", Instant::class)
            prop("schemaVersion", "SchemaVersion")
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
            prop("schemaVersion", "SchemaVersion")
        }
    }

    val optIn = ClassName("kotlin", "OptIn")

    spec.models.forEach { model ->
        val pkg = "io.availe.models"
        val fb = FileSpec.builder(pkg, model.name)
            .addAnnotation(
                AnnotationSpec.builder(optIn)
                    .useSiteTarget(UseSiteTarget.FILE)
                    .addMember(
                        "%T::class, %T::class",
                        ExperimentalTime::class,
                        ExperimentalUuidApi::class
                    )
                    .build()
            )

        spec.wrappers
            .filter { w -> model.props.any { it.type == ClassName(pkg, w.name) } }
            .forEach { fb.addType(generateValueClass(it)) }

        spec.enums
            .filter { e -> model.props.any { it.type == ClassName(pkg, e.name) } }
            .forEach { fb.addType(generateEnum(it)) }

        fb.addType(
            generateDataClass(model)
                .toBuilder()
                .addAnnotation(Serializable::class)
                .build()
        )

        fb.build().writeTo(out)
    }
}
