// In ValueClassBuilder.kt

package io.availe.builders

import com.squareup.kotlinpoet.*
import io.availe.models.Model
import io.availe.models.Property

fun buildValueClass(model: Model, prop: Property.Property): TypeSpec {
    val className = model.name + prop.name.replaceFirstChar { it.uppercaseChar() }

    val underlyingTypeName = ClassName.bestGuess(prop.underlyingType)

    val builder = TypeSpec.classBuilder(className)
        .addAnnotation(JvmInline::class)
        .addModifiers(KModifier.VALUE)
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter(prop.name, underlyingTypeName)
                .build()
        )
        .addProperty(
            PropertySpec.builder(prop.name, underlyingTypeName)
                .initializer(prop.name)
                .build()
        )

    if (model.contextual) {
        builder.addAnnotation(ClassName("kotlinx.serialization", "Serializable"))
    }

    return builder.build()
}