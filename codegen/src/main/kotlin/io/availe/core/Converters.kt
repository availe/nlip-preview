package io.availe.core

import com.squareup.kotlinpoet.*

const val packageName: String = "io.availe.models"

fun convertToPropertySpec(property: Property): PropertySpec {
    return when (property) {
        is Property.Property -> PropertySpec.builder(property.name, property.underlyingType)
            .initializer(property.name)
            .build()

        is Property.ForeignProperty -> PropertySpec.builder(property.name, property.property.underlyingType)
            .initializer(property.name)
            .build()
    }
}

private fun resolvedTypeName(modelParameter: Model, property: Property, variant: Variant): TypeName {
    val suffix: String = variant.suffix
    return when (property) {
        is Property.Property ->
            // e.g. Model.name = "User", prop.name = "id"  →  "UserId"
            ClassName(
                packageName = packageName,
                modelParameter.name + property.name.replaceFirstChar { it.uppercaseChar() }
            )

        is Property.ForeignProperty ->
            // e.g. prop.name = "message" → "Message"
            ClassName(
                packageName = packageName,
                property.name.replaceFirstChar { it.uppercaseChar() + suffix }
            )
    }
}

fun generateDataClass(modelParameter: Model, propertyList: List<Property>, variant: Variant): TypeSpec {
    val constructorBuilder = FunSpec.constructorBuilder().apply {
        propertyList.forEach { propertyItem ->
            addParameter(
                propertyItem.name,
                resolvedTypeName(modelParameter, propertyItem, variant = variant)
            )
        }
    }.build()

    val propertySpecs = propertyList.map { propertyItem ->
        PropertySpec.builder(
            propertyItem.name,
            resolvedTypeName(modelParameter, propertyItem, variant = variant)
        )
            .initializer(propertyItem.name)
            .build()
    }

    return TypeSpec.classBuilder(modelParameter.name)
        .addModifiers(KModifier.DATA)
        .primaryConstructor(constructorBuilder)
        .addProperties(propertySpecs)
        .build()
}

fun fieldsForBase(modelParameter: Model): List<Property> =
    modelParameter.properties

fun fieldsForCreate(modelParameter: Model): List<Property> =
    modelParameter.properties.filter { propertyItem ->
        propertyItem.replication == Replication.CREATE || propertyItem.replication == Replication.BOTH
    }

fun fieldsForPatch(modelParameter: Model): List<Property> =
    modelParameter.properties.filter { propertyItem ->
        propertyItem.replication == Replication.PATCH || propertyItem.replication == Replication.BOTH
    }
