package io.availe.core

import com.squareup.kotlinpoet.*

const val packageName = "io.availe.models"

fun convertToPropertySpec(prop: Property): PropertySpec {
    return when (prop) {
        is Property.Property -> PropertySpec.builder(prop.name, prop.underlyingType)
            .initializer(prop.name).build()

        is Property.ForeignProperty -> PropertySpec.builder(prop.name, prop.property.underlyingType)
            .initializer(prop.name).build()
    }
}

fun fieldsForBase(model: Model): List<Property> = model.properties

fun fieldsForCreate(model: Model): List<Property> =
    model.properties.filter { it.replication == Replication.CREATE || it.replication == Replication.BOTH }

fun fieldsForPatch(model: Model): List<Property> =
    model.properties.filter { it.replication == Replication.PATCH || it.replication == Replication.BOTH }

private fun resolvedTypeName(model: Model, prop: Property): TypeName =
    when (prop) {
        is Property.Property ->
            // e.g. Model.name = "User", prop.name = "id"  →  "UserId"
            ClassName(
                packageName = packageName,
                model.name + prop.name.replaceFirstChar { it.uppercaseChar() }
            )

        is Property.ForeignProperty ->
            // e.g. prop.name = "message" → "Message"
            ClassName(
                packageName = packageName,
                prop.name.replaceFirstChar { it.uppercaseChar() }
            )
    }

fun generateDataClass(model: Model, properties: List<Property>): TypeSpec {
    val constructorBuilder = FunSpec.constructorBuilder().apply {
        properties.forEach { property ->
            addParameter(property.name, resolvedTypeName(model, property))
        }
    }.build()

    val propertySpecs = properties.map { property ->
        PropertySpec.builder(property.name, resolvedTypeName(model, property))
            .initializer(property.name)
            .build()
    }

    return TypeSpec.classBuilder(model.name)
        .addModifiers(KModifier.DATA)
        .primaryConstructor(constructorBuilder)
        .addProperties(propertySpecs)
        .build()
}
