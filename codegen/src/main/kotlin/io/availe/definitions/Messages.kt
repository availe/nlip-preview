package io.availe.definitions

import io.availe.core.codegen
import io.availe.core.generateDataClass

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
    spec.models.forEach { model ->
        writeShared(model.name, generateDataClass(model, spec.wrappers))
    }
}
