package io.availe.definitions

import io.availe.core.Module
import io.availe.core.codegen

fun generateConnectionAggregateModels() {
    val spec = codegen {
        enum("UserSubscriptionTier", listOf("standard", "byok", "enterprise"), nestedIn = "UserAccount")
        model("ConnectionLocationAggregate", module = Module.SERVER) {
            prop("bucketDate", "BucketDate")
            prop("countryCode", "CountryCode")
            prop("regionCode", "RegionCode")
            prop("platform", "PlatformType")
            prop("subscriptionTier", "UserSubscriptionTier")
            prop("accessType", "UserAccessType")
            prop("connectionCount", "ConnectionCount")
            prop("schemaVersion", "ConnectionLocationAggregateSchemaVersion")
        }
    }
    writeServerModels(spec, onlyMain = true)
}
