package io.availe.builders

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.availe.MODELS_PACKAGE_NAME
import io.availe.PATCHABLE_CLASS_NAME
import io.availe.SCHEMA_VERSION_PROPERTY_NAME
import io.availe.UNCHANGED_OBJECT_NAME
import io.availe.models.*

fun buildDataTransferObjectClass(
    model: Model,
    properties: List<Property>,
    variant: Variant,
    valueClassNames: Map<Pair<String, String>, String>,
    existingValueClasses: Set<String>
): TypeSpec {
    val generatedClassName = if (model.isVersionOf != null) variant.suffix else model.name + variant.suffix

    val typeSpecBuilder = TypeSpec.classBuilder(generatedClassName)
        .addModifiers(KModifier.DATA)

    model.annotations?.forEach { ann -> typeSpecBuilder.addAnnotation(buildModelAnnotationSpec(ann)) }

    if (variant == Variant.BASE && model.isVersionOf != null) {
        val schemaName = model.isVersionOf + "Schema"
        typeSpecBuilder.superclass(ClassName(model.packageName, schemaName, model.name))
    }

    val constructorBuilder = FunSpec.constructorBuilder()

    properties.forEach { property ->
        val typeName = resolveTypeNameForProperty(property, variant, model, valueClassNames, existingValueClasses)

        val paramBuilder = ParameterSpec.builder(property.name, typeName)

        if (property.name == SCHEMA_VERSION_PROPERTY_NAME && variant != Variant.PATCH) {
            val unwrapped = if (typeName is ParameterizedTypeName) typeName.typeArguments.first() else typeName
            paramBuilder.defaultValue("%T(%L)", unwrapped, model.schemaVersion)
        }

        if (variant == Variant.PATCH) {
            paramBuilder.defaultValue(
                "%T.%L",
                ClassName(MODELS_PACKAGE_NAME, PATCHABLE_CLASS_NAME),
                UNCHANGED_OBJECT_NAME
            )
        }

        constructorBuilder.addParameter(paramBuilder.build())
        typeSpecBuilder.addProperty(PropertySpec.builder(property.name, typeName).initializer(property.name).build())
    }

    typeSpecBuilder.primaryConstructor(constructorBuilder.build())
    return typeSpecBuilder.build()
}

private fun resolveTypeNameForProperty(
    property: Property,
    variant: Variant,
    model: Model,
    valueClassNames: Map<Pair<String, String>, String>,
    existingValueClasses: Set<String>
): TypeName {
    if (property is Property.ForeignProperty) {
        val baseTypeNameString = property.typeInfo.toTypeName().toString()
        val variantSuffix = when (variant) {
            Variant.BASE -> ".Data"
            Variant.CREATE -> ".CreateRequest"
            Variant.PATCH -> ".PatchRequest"
        }
        val finalTypeName = ClassName.bestGuess("$baseTypeNameString$variantSuffix")

        return if (variant == Variant.PATCH) {
            ClassName(MODELS_PACKAGE_NAME, PATCHABLE_CLASS_NAME).parameterizedBy(finalTypeName)
        } else {
            finalTypeName
        }
    }

    val skipWrapping = property.typeInfo.isEnum ||
            property.typeInfo.isValueClass ||
            property.typeInfo.isDataClass ||
            existingValueClasses.contains(property.typeInfo.qualifiedName) ||
            property.typeInfo.qualifiedName.startsWith("kotlin.collections.")

    val baseType: TypeName = if (skipWrapping) {
        property.typeInfo.toTypeName()
    } else {
        val vcName = valueClassNames[model.name to property.name] ?: return property.typeInfo.toTypeName()
        ClassName(model.packageName, vcName)
    }

    return if (variant == Variant.PATCH) {
        ClassName(MODELS_PACKAGE_NAME, PATCHABLE_CLASS_NAME).parameterizedBy(baseType)
    } else baseType
}

private fun buildModelAnnotationSpec(annotationModel: AnnotationModel): AnnotationSpec {
    val className = ClassName(
        annotationModel.qualifiedName.substringBeforeLast('.'),
        annotationModel.qualifiedName.substringAfterLast('.')
    )
    val builder = AnnotationSpec.builder(className)
    annotationModel.arguments.forEach { (argName, argVal) ->
        when (argVal) {
            is AnnotationArgument.StringValue -> builder.addMember("%L = %S", argName, argVal.value)
            is AnnotationArgument.LiteralValue -> builder.addMember("%L = %L", argName, argVal.value)
        }
    }
    return builder.build()
}