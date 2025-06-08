package io.availe.definitions

import com.squareup.kotlinpoet.FileSpec
import io.availe.core.Module
import io.availe.core.codegen
import io.availe.core.generateDataClass
import java.io.File

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
        val dir = File("../server/build/generated-src/kotlin-poet/io/availe/models").apply { mkdirs() }
        FileSpec.builder("io.availe.models", model.name)
            .addType(generateDataClass(model))
            .build()
            .writeTo(dir)
    }
}
