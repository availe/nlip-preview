package io.availe.definitions

import com.squareup.kotlinpoet.FileSpec
import io.availe.core.codegen
import io.availe.core.generateDataClass
import java.io.File

fun generateMessageModels() {
    val out = File("../shared/build/generated-src/kotlin-poet/io/availe/models").apply { mkdirs() }
    val spec = codegen {
        model("NlipMessage") {
            prop("id", "NlipMessageId")
            prop("format", "AllowedFormatType")
            prop("subformat", "Subformat")
            prop("contentText", "ContentText")
            prop("contentJson", "ContentJson")
            prop("createdAt", "CreatedAt")
            prop("updatedAt", "UpdatedAt")
            prop("schemaVersion", "NlipMessageSchemaVersion")
            prop("messageType", "MessageType")
            prop("label", "Label")
        }
        model("NlipSubmessage") {
            prop("id", "NlipSubmessageId")
            prop("nlipMessageId", "NlipMessageId")
            prop("position", "Position")
            prop("format", "AllowedFormatType")
            prop("subformat", "Subformat")
            prop("contentText", "ContentText")
            prop("contentJson", "ContentJson")
            prop("createdAt", "CreatedAt")
            prop("updatedAt", "UpdatedAt")
            prop("schemaVersion", "NlipSubmessageSchemaVersion")
            prop("label", "Label")
        }
        model("NlipMessageAttachment") {
            prop("id", "AttachmentId")
            prop("nlipMessageId", "NlipMessageId")
            prop("fileKey", "FileKey")
            prop("contentType", "ContentType")
            prop("fileSizeBytes", "FileSizeBytes")
            prop("createdAt", "CreatedAt")
            prop("schemaVersion", "NlipMessageAttachmentSchemaVersion")
        }
        model("NlipSubmessageAttachment") {
            prop("id", "AttachmentId")
            prop("nlipSubmessageId", "NlipSubmessageId")
            prop("fileKey", "FileKey")
            prop("contentType", "ContentType")
            prop("fileSizeBytes", "FileSizeBytes")
            prop("createdAt", "CreatedAt")
            prop("schemaVersion", "NlipSubmessageAttachmentSchemaVersion")
        }
        model("InternalMessage") {
            prop("id", "InternalMessageId")
            prop("conversationId", "ConversationId")
            prop("senderType", "SenderType")
            prop("senderId", "SenderId")
            prop("nlipMessageId", "NlipMessageId")
            prop("createdAt", "CreatedAt")
            prop("updatedAt", "UpdatedAt")
            prop("parentMessageId", "InternalMessageId")
            prop("schemaVersion", "InternalMessageSchemaVersion")
        }
    }
    spec.models.forEach { model ->
        FileSpec.builder("io.availe.models", model.name)
            .addType(generateDataClass(model))
            .build()
            .writeTo(out)
    }
}
