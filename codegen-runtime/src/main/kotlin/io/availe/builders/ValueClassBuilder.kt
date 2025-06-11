package io.availe.builders

import com.squareup.kotlinpoet.*
import io.availe.models.Model
import io.availe.models.Property

private fun String.asClassName(): ClassName {
    val clean = substringBefore('<').removeSuffix("?")
    val pkg = clean.substringBeforeLast('.')
    val type = clean.substringAfterLast('.')
    return ClassName(pkg, type)
}

fun buildValueClass(model: Model, prop: Property.Property): TypeSpec {
    val className = model.name + prop.name.replaceFirstChar { it.uppercaseChar() }
    val underlyingTypeName = prop.underlyingType.asClassName()
    return TypeSpec.classBuilder(className)
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
        .apply {
            if (model.contextual) {
                addAnnotation(ClassName("kotlinx.serialization", "Serializable"))
            }
        }
        .build()
}
