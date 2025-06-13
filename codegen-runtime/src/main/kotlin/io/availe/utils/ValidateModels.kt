package io.availe.utils

import io.availe.models.Model
import io.availe.models.Property
import io.availe.models.Replication
import io.availe.models.Variant

fun validateModelReplications(allModels: List<Model>) {
    val modelsByName = allModels.associateBy { it.name }
    val validationErrors = mutableListOf<String>()

    allModels.forEach { model ->
        if (model.replication == Replication.CREATE || model.replication == Replication.BOTH) {
            if (fieldsForCreate(model).isEmpty()) {
                validationErrors.add(createEmptyVariantError(model, Variant.CREATE))
            }
        }
        if (model.replication == Replication.PATCH || model.replication == Replication.BOTH) {
            if (fieldsForPatch(model).isEmpty()) {
                validationErrors.add(createEmptyVariantError(model, Variant.PATCH))
            }
        }

        model.properties
            .filterIsInstance<Property.ForeignProperty>()
            .forEach { foreignProperty ->
                val targetModelName = foreignProperty.foreignModelName
                val targetModel = modelsByName[targetModelName]
                    ?: error("Unknown referenced model '$targetModelName' in ${model.name}")

                if (model.replication.supports(Variant.CREATE) && fieldsForCreate(model).contains(foreignProperty)) {
                    if (!targetModel.replication.supports(Variant.CREATE) || fieldsForCreate(targetModel).isEmpty()) {
                        validationErrors.add(createDependencyError(model, foreignProperty, targetModel, Variant.CREATE))
                    }
                }

                if (model.replication.supports(Variant.PATCH) && fieldsForPatch(model).contains(foreignProperty)) {
                    if (!targetModel.replication.supports(Variant.PATCH) || fieldsForPatch(targetModel).isEmpty()) {
                        validationErrors.add(createDependencyError(model, foreignProperty, targetModel, Variant.PATCH))
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

private fun createEmptyVariantError(model: Model, variant: Variant): String {
    return """
    Model '${model.name}' has invalid configuration.
    It is declared to support the ${variant.name} variant (model replication is '${model.replication}'), but it contains no properties marked for ${variant.name} or BOTH replication. This would result in an empty '${model.name}${variant.suffix}' class.
    """.trimIndent()
}

private fun createDependencyError(
    parentModel: Model,
    violatingProperty: Property.ForeignProperty,
    targetModel: Model,
    variant: Variant
): String {
    val parentVariantClassName = "${parentModel.name}${variant.suffix}"
    val nestedVariantClassName = "${targetModel.name}${variant.suffix}"
    return """
    Cannot generate '$parentVariantClassName': required nested model '$nestedVariantClassName' cannot be generated.

    Details:
      Parent Model      : ${parentModel.name} (replication: ${parentModel.replication})
      Variant Requested : ${variant.name}
      Nested Property   : ${violatingProperty.name} (type: ${targetModel.name})

    Why:
      The parent model '${parentModel.name}' is configured to generate a '${variant.name}' variant.
      This variant includes the property '${violatingProperty.name}', which refers to the model '${targetModel.name}'.
      However, '${targetModel.name}' (replication: ${targetModel.replication}) does not support generating a non-empty '${variant.name}' variant.
      
      To fix this, either change the replication of '${targetModel.name}' to support '${variant.name}', or adjust the replication of the '${violatingProperty.name}' property.
    """.trimIndent()
}

private fun Replication.supports(variant: Variant): Boolean = when (variant) {
    Variant.BASE -> true
    Variant.CREATE -> this == Replication.CREATE || this == Replication.BOTH
    Variant.PATCH -> this == Replication.PATCH || this == Replication.BOTH
}