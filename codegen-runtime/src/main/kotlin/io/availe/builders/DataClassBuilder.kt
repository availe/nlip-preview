package io.availe.builders

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.availe.models.Model
import io.availe.models.Property
import io.availe.models.Variant

const val packageName: String = "io.availe.models"
private const val BASE_IMPL_SUFFIX = "Data"

private fun baseImplSuffixFor(variant: Variant): String =
    if (variant == Variant.BASE) BASE_IMPL_SUFFIX else variant.suffix

fun resolvedTypeName(model: Model, prop: Property, variant: Variant): TypeName {
    val suffix = baseImplSuffixFor(variant)

    val rawType = when (prop) {
        is Property.Property -> ClassName(
            packageName,
            model.name + prop.name.replaceFirstChar { it.uppercaseChar() }
        )

        is Property.ForeignProperty -> ClassName(
            packageName,
            prop.name.replaceFirstChar { it.uppercaseChar() } + suffix
        )
    }

    return if (prop.optional) {
        ClassName("arrow.core", "Option").parameterizedBy(rawType)
    } else {
        rawType
    }
}

fun dataClassBuilder(model: Model, props: List<Property>, variant: Variant): TypeSpec {
    val className = model.name + variant.suffix
    val typeSpecBuilder = TypeSpec.classBuilder(className)
        .addModifiers(KModifier.DATA)

    val constructorBuilder = FunSpec.constructorBuilder()
    props.forEach { prop ->
        val propType = resolvedTypeName(model, prop, variant)
        constructorBuilder.addParameter(prop.name, propType)
    }
    typeSpecBuilder.primaryConstructor(constructorBuilder.build())

    val shouldImplementInterface = variant == Variant.BASE || variant == Variant.CREATE

    props.forEach { prop ->
        val propType = resolvedTypeName(model, prop, variant)
        val propSpec = PropertySpec.builder(prop.name, propType)
            .initializer(prop.name)

        if (shouldImplementInterface) {
            propSpec.addModifiers(KModifier.OVERRIDE)
        }

        if (model.contextual && prop.optional) {
            propSpec.addAnnotation(
                AnnotationSpec.builder(ClassName("kotlinx.serialization", "Contextual"))
                    .useSiteTarget(AnnotationSpec.UseSiteTarget.PROPERTY)
                    .build()
            )
        }
        typeSpecBuilder.addProperty(propSpec.build())
    }

    if (shouldImplementInterface) {
        val interfaceName = ClassName(packageName, "I${model.name}")
        val typeArguments = props.map { resolvedTypeName(model, it, variant) }
        typeSpecBuilder.addSuperinterface(interfaceName.parameterizedBy(typeArguments))
    }

    if (model.contextual) {
        typeSpecBuilder.addAnnotation(ClassName("kotlinx.serialization", "Serializable"))
    }

    return typeSpecBuilder.build()
}