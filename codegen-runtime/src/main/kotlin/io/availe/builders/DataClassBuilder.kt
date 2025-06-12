package io.availe.builders

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.availe.models.*

const val packageName: String = "io.availe.models"

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

fun resolvedTypeName(prop: Property, variant: Variant, model: Model): TypeName {
    val baseName = model.isVersionOf ?: model.name

    val baseType = when (prop) {
        is Property.Property -> {
            val valueClassName = "$baseName${model.name}${prop.name.replaceFirstChar { it.uppercaseChar() }}"
            ClassName(packageName, valueClassName)
        }
        is Property.ForeignProperty -> {
            val suffix = if (variant == Variant.BASE) "Data" else variant.suffix
            ClassName(packageName, prop.foreignModelName + suffix)
        }
    }

    return if (variant == Variant.PATCH) {
        ClassName(packageName, "Patchable").parameterizedBy(baseType)
    } else {
        baseType
    }
}


fun dataClassBuilder(model: Model, props: List<Property>, variant: Variant): TypeSpec {
    val name = when(variant) {
        Variant.BASE -> "Data"
        Variant.CREATE -> "CreateRequest"
        Variant.PATCH -> "PatchRequest"
    }

    val typeSpec = TypeSpec.classBuilder(name).addModifiers(KModifier.DATA)
    model.annotations?.forEach {
        typeSpec.addAnnotation(buildAnnotation(it))
    }

    val baseModelName = model.isVersionOf
    if (variant == Variant.BASE && baseModelName != null) {
        val schemaName = baseModelName + "Schema"
        typeSpec.superclass(ClassName(packageName, schemaName, model.name))
    }

    val ctor = FunSpec.constructorBuilder()

    val isVersioned = model.isVersionOf != null
    props.forEach { p ->
        val t = resolvedTypeName(p, variant, model)
        val param = ParameterSpec.builder(p.name, t)

        if (isVersioned && p.name == "schemaVersion") {
            if (variant != Variant.PATCH) {
                param.defaultValue("%T(%L)", t, model.schemaVersion)
            }
        }

        if (variant == Variant.PATCH) {
            param.defaultValue("%T.Unchanged", ClassName(packageName, "Patchable"))
        }

        ctor.addParameter(param.build())
    }
    typeSpec.primaryConstructor(ctor.build())

    props.forEach { p ->
        val t = resolvedTypeName(p, variant, model)
        val propSpec = PropertySpec.builder(p.name, t).initializer(p.name)

        p.annotations?.forEach {
            propSpec.addAnnotation(buildAnnotation(it))
        }
        typeSpec.addProperty(propSpec.build())
    }

    return typeSpec.build()
}