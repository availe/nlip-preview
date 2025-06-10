package io.availe.core

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

const val packageName: String = "io.availe.models"

fun generateValueClasses(modelParameter: Model): List<TypeSpec> =
    modelParameter.properties.mapNotNull { prop ->
        if (prop is Property.Property) {
            val className = modelParameter.name +
                    prop.name.replaceFirstChar { it.uppercaseChar() }
            TypeSpec.classBuilder(className)
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
                .build()
        } else null
    }

fun resolvedTypeName(modelParameter: Model, property: Property, variant: Variant): TypeName {
    val suffix: String = variant.suffix
    val type = when (property) {
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
                property.name.replaceFirstChar { it.uppercaseChar() } + suffix
            )
    }

    return if (property.optional) {
        ClassName("arrow.core", "Option")
            .parameterizedBy(type)
    } else {
        type
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
