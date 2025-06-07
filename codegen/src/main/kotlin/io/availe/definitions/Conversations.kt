package io.availe.definitions

import com.squareup.kotlinpoet.FileSpec
import io.availe.core.codegen
import io.availe.core.generateDataClass
import java.io.File

fun generateConversationModels() {
    val out = File("../shared/build/generated-src/kotlin-poet/io/availe/models").apply { mkdirs() }
    val spec = codegen {
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
        FileSpec.builder("io.availe.models", model.name)
            .addType(generateDataClass(model))
            .build()
            .writeTo(out)
    }
}
