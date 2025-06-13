package io.availe.generators

import com.squareup.kotlinpoet.*
import io.availe.INTERFACE_PREFIX
import io.availe.OUTPUT_DIRECTORY
import io.availe.TYPE_VARIABLE_PREFIX
import io.availe.models.Model
import io.availe.utils.fieldsForInterface

/** ------------- File no longer used ------------- */

fun generateInterfaces(models: List<Model>) {
    models.forEach { model ->
        val interfaceSpec = buildGenericInterface(model)
        val interfaceFile = FileSpec.builder(model.packageName, "$INTERFACE_PREFIX${model.name}")
            .addType(interfaceSpec)
            .build()
        interfaceFile.writeTo(OUTPUT_DIRECTORY)
    }
}

private fun buildGenericInterface(model: Model): TypeSpec {
    val properties = fieldsForInterface(model)
    val interfaceBuilder = TypeSpec.interfaceBuilder("$INTERFACE_PREFIX${model.name}")

    if (properties.isEmpty()) {
        return interfaceBuilder.build()
    }

    val typeVariables =
        properties.map { TypeVariableName("$TYPE_VARIABLE_PREFIX${it.name.uppercase()}", variance = KModifier.OUT) }
    return interfaceBuilder
        .addTypeVariables(typeVariables)
        .apply {
            properties.forEachIndexed { index, property ->
                addProperty(PropertySpec.builder(property.name, typeVariables[index]).build())
            }
        }
        .build()
}