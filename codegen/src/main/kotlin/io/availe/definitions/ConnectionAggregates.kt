package io.availe.definitions

import io.availe.core.Module
import io.availe.core.codegen
import io.availe.core.generateDataClass

fun generateConnectionAggregateModels() {
    val spec = codegen {
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
    spec.models.forEach { model ->
        writeServer(model.name, generateDataClass(model))
    }
}
