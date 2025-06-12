package io.availe.builders

import com.squareup.kotlinpoet.*
import io.availe.models.AnnotationArgument
import io.availe.models.AnnotationModel
import io.availe.models.Model
import io.availe.models.Property

private fun String.asClassName(): ClassName {
    val clean = substringBefore('<').removeSuffix("?")
    val pkg = clean.substringBeforeLast('.')
    val type = clean.substringAfterLast('.')
    return ClassName(pkg, type)
}

private fun buildAnnotation(annModel: AnnotationModel): AnnotationSpec {
    val annClassName = annModel.qualifiedName.asClassName()
    val builder = AnnotationSpec.builder(annClassName)
    annModel.arguments.forEach { (key, arg) ->
        when (arg) {
            is AnnotationArgument.StringValue -> builder.addMember("%L = %S", key, arg.value)
            is AnnotationArgument.LiteralValue -> builder.addMember("%L = %L", key, arg.value)
        }
    }
    return builder.build()
}

fun buildValueClass(model: Model, prop: Property.Property): TypeSpec {
    val className = model.name + prop.name.replaceFirstChar { it.uppercaseChar() }
    val underlyingTypeName = prop.underlyingType.asClassName()
    val isParentSerializable = model.annotations?.any { it.qualifiedName == "kotlinx.serialization.Serializable" } == true

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
            if (isParentSerializable) {
                addAnnotation(ClassName("kotlinx.serialization", "Serializable"))
            }
            prop.annotations?.forEach { annotation ->
                addAnnotation(buildAnnotation(annotation))
            }
        }
        .build()
}