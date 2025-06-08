package io.availe.definitions

import io.availe.core.codegen

fun generateUserAccountModels() {
    val spec = codegen {
        enum("UserSubscriptionTier", listOf("standard", "byok", "enterprise"), nestedIn = "UserAccount")
        model("UserAccount") {
            prop("id", "UserAccountId", inCreate = false, inPatch = false)
            prop("username", "Username")
            prop("emailAddress", "EmailAddress")
            prop("accountIsActive", "AccountIsActive")
            prop("subscriptionTier", "UserSubscriptionTier")
            prop("schemaVersion", "UserAccountSchemaVersion", inCreate = true, inPatch = true)
        }
    }
    writeSharedModels(spec, includeNestedEnums = true)
}
