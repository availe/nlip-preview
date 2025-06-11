package io.availe.utils

import io.availe.models.*

fun validateModelReplications(models: List<Model>) {
    val repByName = models.associate { it.name to it.replication }

    models.forEach { model ->
        listOf(
            Variant.BASE to Replication.NONE,
            Variant.CREATE to Replication.CREATE,
            Variant.PATCH to Replication.PATCH
        ).forEach { (variant, needed) ->
            if (!model.replication.allowedVariants(needed)) return@forEach

            model.properties
                .filterIsInstance<Property.ForeignProperty>()
                .forEach { fp ->
                    val targetName = fp.name.replaceFirstChar { it.uppercaseChar() }
                    val targetReplication = repByName[targetName]
                        ?: error("Unknown referenced model '$targetName' in ${model.name}")

                    if (!targetReplication.allowedVariants(needed)) {
                        val parentVariantClass = "${model.name}${variant.suffix}"
                        val nestedVariantClass = "${targetName}${variant.suffix}"
                        val errorMessage = """
                            Cannot generate '$parentVariantClass': required nested model '$nestedVariantClass' cannot be generated.

                            Details:
                              Parent Model       : ${model.name}
                              Variant Requested  : ${variant.name}
                              Nested Property    : ${fp.name} (type: $targetName)

                            Why:
                              '$targetName' does not support the ${variant.name} variant.
                              Supported variants for '$targetName': { ${targetReplication.printAllowedVariants()} }

                            → Because '$parentVariantClass' includes '${fp.name}',
                              it depends on the existence of '$nestedVariantClass', which cannot be generated.
                        """.trimIndent()
                        error(errorMessage)
                    }
                }
        }
    }
}

