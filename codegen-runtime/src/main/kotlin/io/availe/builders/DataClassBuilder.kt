package io.availe.builders

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.availe.models.*

fun buildDataTransferObjectClass(
    model: Model,
    properties: List<Property>,
    variant: Variant,
    valueClassNames: Map<Pair<String, String>, String>
): TypeSpec {
    val generatedClassName = if (model.isVersionOf != null) {
        variant.suffix
    } else {
        model.name + variant.suffix
    }
    val typeSpecBuilder = TypeSpec.classBuilder(generatedClassName)
        .addModifiers(KModifier.DATA)

    model.annotations?.forEach { annotationModel ->
        typeSpecBuilder.addAnnotation(buildModelAnnotationSpec(annotationModel))
    }

    if (variant == Variant.BASE && model.isVersionOf != null) {
        val schemaName = model.isVersionOf + "Schema"
        typeSpecBuilder.superclass(ClassName(model.packageName, schemaName, model.name))
    }

    val constructorBuilder = FunSpec.constructorBuilder()

    properties.forEach { property ->
        val typeName = resolveTypeNameForProperty(property, variant, model, valueClassNames)
        val parameterBuilder = ParameterSpec.builder(property.name, typeName)

        if (property.name == "schemaVersion" && variant != Variant.PATCH) {
            parameterBuilder.defaultValue("%T(%L)", typeName, model.schemaVersion)
        }

        if (variant == Variant.PATCH) {
            parameterBuilder.defaultValue("%T.Unchanged", ClassName(model.packageName, "Patchable"))
        }

        constructorBuilder.addParameter(parameterBuilder.build())
        typeSpecBuilder.addProperty(PropertySpec.builder(property.name, typeName).initializer(property.name).build())
    }

    typeSpecBuilder.primaryConstructor(constructorBuilder.build())
    return typeSpecBuilder.build()
}

private fun resolveTypeNameForProperty(
    property: Property,
    variant: Variant,
    model: Model,
    valueClassNames: Map<Pair<String, String>, String>
): TypeName {
    val baseType = when (property) {
        is Property.Property -> {
            val valueClassName = valueClassNames[model.name to property.name]
                ?: error("Could not find value class name for ${model.name}.${property.name}")
            ClassName(model.packageName, valueClassName)
        }

        is Property.ForeignProperty -> {
            val suffix = if (variant == Variant.BASE) "Data" else variant.suffix
            ClassName(model.packageName, property.foreignModelName + suffix)
        }
    }

    return if (variant == Variant.PATCH) {
        ClassName(model.packageName, "Patchable").parameterizedBy(baseType)
    } else {
        baseType
    }
}

private fun buildModelAnnotationSpec(annotationModel: AnnotationModel): AnnotationSpec {
    val annotationClassName = ClassName(
        annotationModel.qualifiedName.substringBeforeLast('.'),
        annotationModel.qualifiedName.substringAfterLast('.')
    )
    val builder = AnnotationSpec.builder(annotationClassName)
    annotationModel.arguments.forEach { (argumentName, argumentValue) ->
        when (argumentValue) {
            is AnnotationArgument.StringValue -> builder.addMember("%L = %S", argumentName, argumentValue.value)
            is AnnotationArgument.LiteralValue -> builder.addMember("%L = %L", argumentName, argumentValue.value)
        }
    }
    return builder.build()
}