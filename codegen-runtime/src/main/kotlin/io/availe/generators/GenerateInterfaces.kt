package io.availe.generators

import com.squareup.kotlinpoet.*
import io.availe.models.Model
import io.availe.utils.fieldsForInterface
import java.io.File

fun generateInterfaces(models: List<Model>) {
    val outputDirectory = File("build/generated-src/kotlin-poet")
    models.forEach { model ->
        val interfaceSpec = buildGenericInterface(model)
        val interfaceFile = FileSpec.builder(model.packageName, "I${model.name}")
            .addType(interfaceSpec)
            .build()
        interfaceFile.writeTo(outputDirectory)
    }
}

private fun buildGenericInterface(model: Model): TypeSpec {
    val properties = fieldsForInterface(model)
    val interfaceBuilder = TypeSpec.interfaceBuilder("I${model.name}")

    if (properties.isEmpty()) {
        return interfaceBuilder.build()
    }

    val typeVariables = properties.map { TypeVariableName("T_${it.name.uppercase()}", variance = KModifier.OUT) }
    return interfaceBuilder
        .addTypeVariables(typeVariables)
        .apply {
            properties.forEachIndexed { index, property ->
                addProperty(PropertySpec.builder(property.name, typeVariables[index]).build())
            }
        }
        .build()
}