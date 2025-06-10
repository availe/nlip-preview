package io.availe.core

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

const val packageName: String = "io.availe.models"

fun resolvedTypeName(modelParameter: Model, property: Property): TypeName {
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
                property.name.replaceFirstChar { it.uppercaseChar() }
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
                resolvedTypeName(modelParameter, propertyItem)
            )
        }
    }.build()

    val propertySpecs = propertyList.map { propertyItem ->
        val typeName = resolvedTypeName(modelParameter, propertyItem)
        val propertyBuilder = PropertySpec.builder(
            propertyItem.name,
            typeName
        ).initializer(propertyItem.name)

        if (modelParameter.contextual && propertyItem.optional) {
            propertyBuilder.addAnnotation(
                AnnotationSpec.builder(ClassName("kotlinx.serialization", "Contextual"))
                    .useSiteTarget(AnnotationSpec.UseSiteTarget.PROPERTY)
                    .build()
            )
        }

        propertyBuilder.build()
    }

    val typeSpecBuilder = TypeSpec.classBuilder(modelParameter.name)
        .addModifiers(KModifier.DATA)
        .primaryConstructor(constructorBuilder)
        .addProperties(propertySpecs)

    if (modelParameter.contextual) {
        typeSpecBuilder.addAnnotation(ClassName("kotlinx.serialization", "Serializable"))
    }

    return typeSpecBuilder.build()
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
