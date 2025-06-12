package io.availe.utils

import io.availe.models.Model
import io.availe.models.Property
import io.availe.models.Variant

fun validateModelReplications(models: List<Model>) {
    val modelsByName = models.associateBy { it.name }

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

                        ERROR: Cannot generate '$parentVariantClass'.
                        It has a dependency on '$nestedVariantClass', which cannot be generated because it would be empty.

                        DETAILS
                        - Parent Model:         ${model.name} (Property '${fp.name}')
                        - Required Dependency:  $targetName (as a ${variant.name} variant)

                        WHY?
                        The model '$targetName' has no properties that are configured for replication in a '${variant.name}' context.
                        To fix this, please update the @FieldGen annotations within the '$targetName' interface to include '${variant.name}' replication for at least one field.
                        """.trimIndent()
                        error(errorMessage)
                    }
                }
        }
    }
}

private fun fieldsForVariant(model: Model, variant: Variant): List<Property> = when (variant) {
    Variant.BASE -> model.properties
    Variant.CREATE -> fieldsForCreate(model)
    Variant.PATCH -> fieldsForPatch(model)
}