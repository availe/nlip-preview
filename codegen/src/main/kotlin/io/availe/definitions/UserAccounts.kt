package io.availe.definitions

import io.availe.core.*

fun generateUserAccountModels() {
    val spec = codegen {
        enum("UserSubscriptionTier", listOf("standard", "byok", "enterprise"), nestedIn = "UserAccount")
        model("UserAccount") {
            prop("id", "UserAccountId", inCreate = false, inPatch = false)
            prop("username", "Username")
            prop("emailAddress", "EmailAddress")
            prop("accountIsActive", "AccountIsActive")
            prop("subscriptionTier", "UserSubscriptionTier")
            prop("schemaVersion", "UserAccountSchemaVersion", inCreate = false, inPatch = false)
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
