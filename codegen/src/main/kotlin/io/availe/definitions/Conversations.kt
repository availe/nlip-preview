package io.availe.definitions

import io.availe.core.codegen

fun generateConversationModels() {
    val spec = codegen {
        enum("ConversationStatus", listOf("active", "archived", "local", "temporary"), nestedIn = "Conversation")
        model("Conversation") {
            prop("id", "ConversationId", inCreate = false, inPatch = false)
            prop("title", "ConversationTitle")
            prop("createdAt", "CreatedAt", inCreate = false, inPatch = false)
            prop("updatedAt", "UpdatedAt", inCreate = false, inPatch = false)
            prop("ownerId", "UserAccountId", inPatch = false)
            prop("status", "ConversationStatus")
            prop("schemaVersion", "ConversationSchemaVersion", inCreate = true, inPatch = true)
        }
    }
    writeSharedModels(spec, includeNestedEnums = true)
}
