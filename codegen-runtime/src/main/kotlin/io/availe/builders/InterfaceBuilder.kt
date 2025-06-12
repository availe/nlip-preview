package io.availe.builders

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import io.availe.models.Model
import io.availe.utils.fieldsForInterface

fun buildGenericInterface(model: Model): TypeSpec {
    val props = fieldsForInterface(model)
    if (props.isEmpty()) return TypeSpec.interfaceBuilder("I${model.name}").build()

    val vars = props.map { TypeVariableName("T_${it.name.uppercase()}", variance = KModifier.OUT) }
    return TypeSpec.interfaceBuilder("I${model.name}")
        .addTypeVariables(vars)
        .apply {
            props.forEachIndexed { i, p ->
                addProperty(PropertySpec.builder(p.name, vars[i]).build())
            }
        }
        .build()
}
