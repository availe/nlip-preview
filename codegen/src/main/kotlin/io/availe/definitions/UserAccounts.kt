package io.availe.definitions

import com.squareup.kotlinpoet.FileSpec
import io.availe.core.codegen
import io.availe.core.generateDataClass
import java.io.File

fun generateUserAccountModels() {
    val out = File("../shared/build/generated-src/kotlin-poet/io/availe/models").apply { mkdirs() }
    val spec = codegen {
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
        FileSpec.builder("io.availe.models", model.name)
            .addType(generateDataClass(model))
            .build()
            .writeTo(out)
    }
}
