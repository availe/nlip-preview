package io.availe.definitions

import com.squareup.kotlinpoet.FileSpec
import io.availe.core.codegen
import io.availe.core.generateDataClass
import io.availe.core.generateEnum
import java.io.File

fun generateUserAccountModels() {
    val spec = codegen {
        enum(
            "UserSubscriptionTier",
            listOf("standard", "byok", "enterprise"),
            nestedIn = "UserAccount"
        )
        model("UserAccount") {
            prop("id", "UserAccountId")
            prop("username", "Username")
            prop("emailAddress", "EmailAddress")
            prop("accountIsActive", "AccountIsActive")
            prop("subscriptionTier", "UserSubscriptionTier")
            prop("schemaVersion", "UserAccountSchemaVersion")
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
