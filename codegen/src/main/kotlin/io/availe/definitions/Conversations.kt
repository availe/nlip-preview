package io.availe.definitions

import io.availe.core.*

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
            prop("schemaVersion", "ConversationSchemaVersion", inCreate = false, inPatch = false)
        }
    }
    spec.models.forEach { model ->
        val nestedEnums = spec.enums.filter { it.nestedIn == model.name }
        val main = generateDataClass(model, spec.wrappers).toBuilder().apply {
            nestedEnums.forEach { addType(generateEnum(it)) }
        }.build()
        val create = generateCreateClass(model, spec.wrappers)
        val patch = generatePatchClass(model, spec.wrappers)
        writeShared(model.name, main, create, patch)
    }
}
