package io.availe.builders

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import io.availe.models.Model

fun buildGenericInterface(model: Model): TypeSpec {
    val interfaceName = "I${model.name}"
    val interfaceBuilder = TypeSpec.interfaceBuilder(interfaceName)

    val typeVariables = model.properties.map { prop ->
        TypeVariableName("T_${prop.name.uppercase()}", variance = KModifier.OUT)
    }
    interfaceBuilder.addTypeVariables(typeVariables)

    model.properties.forEachIndexed { index, prop ->
        interfaceBuilder.addProperty(
            PropertySpec.builder(prop.name, typeVariables[index]).build()
        )
    }

    return interfaceBuilder.build()
}