package io.availe.definitions

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.availe.core.codegen

fun generateMessageModels() {
    val LIST = ClassName("kotlin.collections", "List")
    val nlipMsgAttachList = LIST.parameterizedBy(ClassName("io.availe.models", "NlipMessageAttachment"))
    val nlipSubmsgAttachList = LIST.parameterizedBy(ClassName("io.availe.models", "NlipSubmessageAttachment"))
    val nlipSubmsgList = LIST.parameterizedBy(ClassName("io.availe.models", "NlipSubmessage"))

    val spec = codegen {
        model("NlipMessageAttachment") {
            prop("id", "AttachmentId", inCreate = false, inPatch = false)
            prop("nlipMessageId", "NlipMessageId", inPatch = false)
            prop("fileKey", "FileKey")
            prop("contentType", "ContentType")
            prop("fileSizeBytes", "FileSizeBytes")
            prop("createdAt", "CreatedAt", inCreate = false, inPatch = false)
            prop("schemaVersion", "NlipMessageAttachmentSchemaVersion", inCreate = false, inPatch = false)
        }
        model("NlipSubmessageAttachment") {
            prop("id", "AttachmentId", inCreate = false, inPatch = false)
            prop("nlipSubmessageId", "NlipSubmessageId", inPatch = false)
            prop("fileKey", "FileKey")
            prop("contentType", "ContentType")
            prop("fileSizeBytes", "FileSizeBytes")
            prop("createdAt", "CreatedAt", inCreate = false, inPatch = false)
            prop("schemaVersion", "NlipSubmessageAttachmentSchemaVersion", inCreate = false, inPatch = false)
        }
        model("NlipSubmessage") {
            prop("id", "NlipSubmessageId", inCreate = false, inPatch = false)
            prop("nlipMessageId", "NlipMessageId", inPatch = false)
            prop("position", "Position")
            prop("format", "AllowedFormatType", inPatch = false)
            prop("subformat", "Subformat")
            prop("contentText", "ContentText", nullable = true)
            prop("contentJson", "ContentJson", nullable = true)
            prop("createdAt", "CreatedAt", inCreate = false, inPatch = false)
            prop("updatedAt", "UpdatedAt", inCreate = false, inPatch = false)
            prop("schemaVersion", "NlipSubmessageSchemaVersion", inCreate = false, inPatch = false)
            prop("label", "Label", nullable = true)
            prop("attachments", nlipSubmsgAttachList)
        }
        model("NlipMessage") {
            prop("id", "NlipMessageId", inCreate = false, inPatch = false)
            prop("format", "AllowedFormatType", inPatch = false)
            prop("subformat", "Subformat")
            prop("contentText", "ContentText", nullable = true)
            prop("contentJson", "ContentJson", nullable = true)
            prop("createdAt", "CreatedAt", inCreate = false, inPatch = false)
            prop("updatedAt", "UpdatedAt", inCreate = false, inPatch = false)
            prop("schemaVersion", "NlipMessageSchemaVersion", inCreate = false, inPatch = false)
            prop("messageType", "MessageType", nullable = true, inPatch = false)
            prop("label", "Label", nullable = true)
            prop("attachments", nlipMsgAttachList)
            prop("submessages", nlipSubmsgList)
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
            prop("schemaVersion", "InternalMessageSchemaVersion", inCreate = true, inPatch = true)
        }
    }
    writeSharedModels(spec)
}
