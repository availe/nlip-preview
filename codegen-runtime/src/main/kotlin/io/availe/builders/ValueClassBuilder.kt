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

private fun buildAnnotation(annModel: AnnotationModel): com.squareup.kotlinpoet.AnnotationSpec {
    val annClassName = annModel.qualifiedName.asClassName()
    val builder = com.squareup.kotlinpoet.AnnotationSpec.builder(annClassName)
    annModel.arguments.forEach { (key, arg) ->
        when (arg) {
            is AnnotationArgument.StringValue -> builder.addMember("%L = %S", key, arg.value)
            is AnnotationArgument.LiteralValue -> builder.addMember("%L = %L", key, arg.value)
        }
    }
    return builder.build()
}

fun buildValueClass(model: Model, prop: Property.Property, isVersioned: Boolean): TypeSpec {
    val className = prop.name.replaceFirstChar { it.uppercaseChar() }
    val underlyingTypeName = if(isVersioned) prop.typeInfo.toTypeName() else prop.typeInfo.toTypeName(model.name)
    val propName = if (prop.name == "schema_version") "schemaVersion" else prop.name
    val isParentSerializable = model.annotations?.any { it.qualifiedName == "kotlinx.serialization.Serializable" } == true

    val ctorParamBuilder = ParameterSpec.builder(propName, underlyingTypeName)
    prop.annotations?.forEach { annotation ->
        ctorParamBuilder.addAnnotation(buildAnnotation(annotation))
    }

    return TypeSpec.classBuilder(className)
        .addAnnotation(JvmInline::class)
        .addModifiers(KModifier.VALUE)
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter(ctorParamBuilder.build())
                .build()
        )
        .addProperty(
            PropertySpec.builder(propName, underlyingTypeName)
                .initializer(propName)
                .build()
        )
        .apply {
            if (isParentSerializable) {
                addAnnotation(ClassName("kotlinx.serialization", "Serializable"))
            }
        }
        .build()
}