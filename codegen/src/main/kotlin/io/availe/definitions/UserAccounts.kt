package io.availe.definitions

import io.availe.core.codegen
import io.availe.core.generateDataClass
import io.availe.core.generateEnum

fun generateUserAccountModels() {
    val spec = codegen {
        enum("UserSubscriptionTier", listOf("standard", "byok", "enterprise"), nestedIn = "UserAccount")
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
        val nestedEnums = spec.enums.filter { it.nestedIn == model.name }
        val type = generateDataClass(model).toBuilder().apply {
            nestedEnums.forEach { addType(generateEnum(it)) }
        }.build()
        writeShared(model.name, type)
    }
}
