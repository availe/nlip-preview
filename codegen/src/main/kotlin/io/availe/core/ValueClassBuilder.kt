package io.availe.core

import com.squareup.kotlinpoet.*

fun buildValueClass(model: Model, prop: Property.Property): TypeSpec {
    val className = model.name + prop.name.replaceFirstChar { it.uppercaseChar() }
    val builder = TypeSpec.classBuilder(className)
        .addAnnotation(JvmInline::class)
        .addModifiers(KModifier.VALUE)
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter(prop.name, prop.underlyingType)
                .build()
        )
        .addProperty(
            PropertySpec.builder(prop.name, prop.underlyingType)
                .initializer(prop.name)
                .build()
        )

    if (model.contextual) {
        builder.addAnnotation(ClassName("kotlinx.serialization", "Serializable"))
    }

    return builder.build()
}
