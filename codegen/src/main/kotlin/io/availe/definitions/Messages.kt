package io.availe.definitions

import com.squareup.kotlinpoet.FileSpec
import io.availe.core.codegen
import io.availe.core.generateDataClass
import java.io.File

fun generateMessageModels() {
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
        model("InternalMessage") {
            prop("id", "InternalMessageId")
            prop("conversationId", "ConversationId")
            prop("senderType", "SenderType")
            prop("senderId", "SenderId")
            prop("nlipMessage", "NlipMessage")
            prop("createdAt", "CreatedAt")
            prop("updatedAt", "UpdatedAt")
            prop("parentMessageId", "InternalMessageId", nullable = true)
            prop("schemaVersion", "InternalMessageSchemaVersion")
        }
        model("OutboundMessage") {
            prop("targetUrl", String::class)
            prop("internalMessage", "InternalMessage")
            prop("schemaVersion", "InternalMessageSchemaVersion")
        }
    }
    val outDir = File("../shared/build/generated-src/kotlin-poet/io/availe/models").apply { mkdirs() }
    spec.models.forEach { model ->
        FileSpec.builder("io.availe.models", model.name)
            .addType(generateDataClass(model))
            .build()
            .writeTo(outDir)
    }
}
