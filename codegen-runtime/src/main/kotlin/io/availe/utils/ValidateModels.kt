package io.availe.utils

import io.availe.models.Model
import io.availe.models.Property
import io.availe.models.Variant
import io.availe.models.printAllowedVariants

fun validateModelReplications(models: List<Model>) {
    val modelsByName = models.associateBy { it.name }
    val validationErrors = mutableListOf<String>()

    models.forEach { model ->
        listOf(Variant.CREATE, Variant.PATCH).forEach { variant ->
            if (fieldsForVariant(model, variant).isEmpty()) {
                return@forEach
            }

            model.properties
                .filterIsInstance<Property.ForeignProperty>()
                .forEach { fp ->
                    val targetName = fp.foreignModelName
                    val targetModel = modelsByName[targetName]
                        ?: error("Unknown referenced model '$targetName' in ${model.name}")

                    val isDependencySatisfied = when (variant) {
                        Variant.CREATE -> fieldsForCreate(targetModel).isNotEmpty()
                        Variant.PATCH -> fieldsForPatch(targetModel).isNotEmpty()
                        else -> true
                    }

                    if (!isDependencySatisfied) {
                        val parentVariantClass = "${model.name}${variant.suffix}"
                        val nestedVariantClass = "${targetName}${variant.suffix}"
                        val errorMessage = """
                        Cannot generate '$parentVariantClass': required nested model '$nestedVariantClass' cannot be generated.

                        Details:
                          Parent Model       : ${model.name}
                          Variant Requested  : ${variant.name}
                          Nested Property    : ${fp.name} (type: $targetName)

                        Why:
                          '$targetName' does not support generating a non-empty ${variant.name} variant.
                          Supported variants for '$targetName': { ${targetModel.replication.printAllowedVariants()} }

                          → Because '$parentVariantClass' includes '${fp.name}',
                            it depends on the existence of '$nestedVariantClass', which cannot be generated.
                        """.trimIndent()
                        validationErrors.add(errorMessage)
                    }
                }
        }
    }

    if (validationErrors.isNotEmpty()) {
        val finalReport = "Model validation failed with ${validationErrors.size} error(s):\n\n" +
                validationErrors.joinToString("\n\n--------------------------------------------------\n\n")
        error(finalReport)
    }
}

private fun fieldsForVariant(model: Model, variant: Variant): List<Property> = when (variant) {
    Variant.BASE -> model.properties
    Variant.CREATE -> fieldsForCreate(model)
    Variant.PATCH -> fieldsForPatch(model)
}