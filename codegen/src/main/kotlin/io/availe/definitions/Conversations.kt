package io.availe.definitions

import com.squareup.kotlinpoet.FileSpec
import io.availe.core.codegen
import io.availe.core.generateDataClass
import io.availe.core.generateEnum
import java.io.File

fun generateConversationModels() {
    val spec = codegen {
        enum(
            "ConversationStatus",
            listOf("active", "archived", "local", "temporary"),
            nestedIn = "Conversation"
        )
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
        val dir = File("../shared/build/generated-src/kotlin-poet/io/availe/models").apply { mkdirs() }
        val nestedEnums = spec.enums.filter { it.nestedIn == model.name }
        val modelType = generateDataClass(model).toBuilder().apply {
            nestedEnums.forEach { addType(generateEnum(it)) }
        }.build()
        FileSpec.builder("io.availe.models", model.name)
            .addType(modelType)
            .build()
            .writeTo(dir)
    }
}
