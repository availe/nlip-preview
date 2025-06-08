package io.availe.definitions

import io.availe.core.codegen
import io.availe.core.generateCreateClass
import io.availe.core.generateDataClass
import io.availe.core.generatePatchClass

fun generateMessageModels() {
    val spec = codegen {
        model("NlipMessage") {
            prop("id", "NlipMessageId", inCreate = false, inPatch = false)
            prop("format", "AllowedFormatType", inPatch = false)
            prop("subformat", "Subformat")
            prop("contentText", "ContentText")
            prop("contentJson", "ContentJson")
            prop("createdAt", "CreatedAt", inCreate = false, inPatch = false)
            prop("updatedAt", "UpdatedAt", inCreate = false, inPatch = false)
            prop("schemaVersion", "NlipMessageSchemaVersion", inCreate = false, inPatch = false)
            prop("messageType", "MessageType", inPatch = false)
            prop("label", "Label")
        }
        model("InternalMessage") {
            prop("id", "InternalMessageId", inCreate = false, inPatch = false)
            prop("conversationId", "ConversationId", inPatch = false)
            prop("senderType", "SenderType", inCreate = false, inPatch = false)
            prop("senderId", "SenderId", inCreate = false, inPatch = false)
            prop("nlipMessage", "NlipMessage")
            prop("createdAt", "CreatedAt", inCreate = false, inPatch = false)
            prop("updatedAt", "UpdatedAt", inCreate = false, inPatch = false)
            prop("parentMessageId", "InternalMessageId", nullable = true)
            prop("schemaVersion", "InternalMessageSchemaVersion", inCreate = false, inPatch = false)
        }
        model("OutboundMessage") {
            prop("targetUrl", String::class)
            prop("internalMessage", "InternalMessage")
            prop("schemaVersion", "InternalMessageSchemaVersion", inCreate = false, inPatch = false)
        }
    }
    spec.models.forEach { model ->
        val main = generateDataClass(model, spec.wrappers)
        val create = generateCreateClass(model, spec.wrappers)
        val patch = generatePatchClass(model, spec.wrappers)
        writeShared(model.name, main, create, patch)
    }
}
