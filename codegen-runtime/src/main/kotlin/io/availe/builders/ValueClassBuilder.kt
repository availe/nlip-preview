package io.availe.builders

import com.squareup.kotlinpoet.*
import io.availe.models.AnnotationArgument
import io.availe.models.AnnotationModel
import io.availe.models.Property

private const val OPT_IN_QUALIFIED_NAME = "kotlin.OptIn"

private fun String.asClassName(): ClassName {
    val cleanName = this.substringBefore('<').removeSuffix("?")
    val packageName = cleanName.substringBeforeLast('.')
    val simpleName = cleanName.substringAfterLast('.')
    return ClassName(packageName, simpleName)
}

private fun buildAnnotationSpec(annotationModel: AnnotationModel): AnnotationSpec {
    val annotationClassName = annotationModel.qualifiedName.asClassName()
    val builder = AnnotationSpec.builder(annotationClassName)
    annotationModel.arguments.forEach { (argumentName, argumentValue) ->
        when (argumentValue) {
            is AnnotationArgument.StringValue -> builder.addMember("%L = %S", argumentName, argumentValue.value)
            is AnnotationArgument.LiteralValue -> builder.addMember("%L = %L", argumentName, argumentValue.value)
        }
    }
    return builder.build()
}

fun buildValueClass(
    className: String,
    property: Property.Property,
    isSerializable: Boolean
): TypeSpec {
    val underlyingTypeName = property.typeInfo.toTypeName()
    val constructorParameterBuilder = ParameterSpec.builder("value", underlyingTypeName)

    property.annotations
        ?.filterNot { it.qualifiedName == OPT_IN_QUALIFIED_NAME }
        ?.forEach { annotation ->
            constructorParameterBuilder.addAnnotation(buildAnnotationSpec(annotation))
        }

    return TypeSpec.classBuilder(className)
        .addAnnotation(JvmInline::class)
        .addModifiers(KModifier.VALUE)
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter(constructorParameterBuilder.build())
                .build()
        )
        .addProperty(
            PropertySpec.builder("value", underlyingTypeName)
                .initializer("value")
                .build()
        )
        .apply {
            if (isSerializable) {
                addAnnotation(ClassName("kotlinx.serialization", "Serializable"))
            }
        }
        .build()
}