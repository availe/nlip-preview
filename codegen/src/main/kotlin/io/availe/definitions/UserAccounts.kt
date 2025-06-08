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
        fun withEnums(ts: com.squareup.kotlinpoet.TypeSpec) =
            ts.toBuilder().apply { nestedEnums.forEach { addType(generateEnum(it)) } }.build()

        writeShared(model.name, withEnums(generateDataClass(model, spec.wrappers)))
        writeShared("${model.name}Create", withEnums(generateCreateClass(model, spec.wrappers)))
        writeShared("${model.name}Patch", withEnums(generatePatchClass(model, spec.wrappers)))
    }
}
