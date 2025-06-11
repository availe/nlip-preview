package io.availe.builders

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.availe.models.Model
import io.availe.models.Property
import io.availe.models.Variant
import kotlin.collections.map

const val packageName: String = "io.availe.models"

fun resolvedTypeName(modelParameter: Model, property: Property, variant: Variant): TypeName {
    val suffix = variant.suffix
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

fun dataClassBuilder(modelParameter: Model, propertyList: List<Property>, variant: Variant): TypeSpec {
    val constructorBuilder = FunSpec.constructorBuilder().apply {
        propertyList.forEach { propertyItem ->
            addParameter(
                propertyItem.name,
                resolvedTypeName(modelParameter, propertyItem, variant)
            )
        }
    }.build()

    val propertySpecs = propertyList.map { propertyItem ->
        val typeName = resolvedTypeName(modelParameter, propertyItem, variant)
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

    val className = modelParameter.name + variant.suffix
    val typeSpecBuilder = TypeSpec.classBuilder(className)
        .addModifiers(KModifier.DATA)
        .primaryConstructor(constructorBuilder)
        .addProperties(propertySpecs)

    if (modelParameter.contextual) {
        typeSpecBuilder.addAnnotation(ClassName("kotlinx.serialization", "Serializable"))
    }

    return typeSpecBuilder.build()
}
