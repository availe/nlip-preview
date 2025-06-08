package io.availe.definitions

import io.availe.core.codegen
import io.availe.core.generateDataClass
import io.availe.core.generateEnum

fun generateConversationModels() {
    val spec = codegen {
        enum("ConversationStatus", listOf("active", "archived", "local", "temporary"), nestedIn = "Conversation")
        model("Conversation") {
            prop("id", "ConversationId")
            prop("title", "ConversationTitle")
            prop("createdAt", "CreatedAt")
            prop("updatedAt", "UpdatedAt")
            prop("ownerId", "UserAccountId")
            prop("status", "ConversationStatus")
            prop("schemaVersion", "ConversationSchemaVersion")
        }
    }
    spec.models.forEach { model ->
        val nestedEnums = spec.enums.filter { it.nestedIn == model.name }
        val type = generateDataClass(model, spec.wrappers).toBuilder().apply {
            nestedEnums.forEach { addType(generateEnum(it)) }
        }.build()
        writeShared(model.name, type)
    }
}
