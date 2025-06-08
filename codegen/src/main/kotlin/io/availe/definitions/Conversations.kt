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
        fun withEnums(ts: com.squareup.kotlinpoet.TypeSpec) =
            ts.toBuilder().apply { nestedEnums.forEach { addType(generateEnum(it)) } }.build()

        writeShared(model.name, withEnums(generateDataClass(model, spec.wrappers)))
        writeShared("${model.name}Create", withEnums(generateCreateClass(model, spec.wrappers)))
        writeShared("${model.name}Patch", withEnums(generatePatchClass(model, spec.wrappers)))
    }
}
