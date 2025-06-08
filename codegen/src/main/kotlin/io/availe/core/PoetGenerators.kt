@file:OptIn(ExperimentalUuidApi::class)

package io.availe.core

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun generateValueClass(wrapper: InlineWrapper): TypeSpec {
    val backingType = wrapper.backing
    val contextualBackings = setOf(
        Instant::class.asTypeName(),
        LocalDate::class.asTypeName(),
        Uuid::class.asTypeName(),
        ClassName("kotlinx.serialization.json", "JsonElement")
    )

    val valueProp = PropertySpec.builder("value", backingType)
        .initializer("value")
        .apply {
            if (backingType in contextualBackings) addAnnotation(Contextual::class)
        }
        .build()

    return TypeSpec.classBuilder(wrapper.name)
        .addModifiers(KModifier.VALUE)
        .addAnnotation(JvmInline::class)
        .addAnnotation(Serializable::class)
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter("value", backingType)
                .build()
        )
        .addProperty(valueProp)
        .build()
}

fun generateEnum(spec: EnumSpec): TypeSpec =
    TypeSpec.enumBuilder(spec.name)
        .addAnnotation(Serializable::class)
        .apply { spec.values.forEach { addEnumConstant(it.uppercase()) } }
        .build()

fun generateDataClass(model: ModelSpec, wrappers: List<InlineWrapper>): TypeSpec {
    val wrapperNames = wrappers.map { it.name }.toSet()

    val ctor = FunSpec.constructorBuilder().apply {
        model.props.forEach { addParameter(it.name, it.type) }
    }.build()

    val builder = TypeSpec.classBuilder(model.name)
        .addModifiers(KModifier.DATA)
        .addAnnotation(Serializable::class)
        .primaryConstructor(ctor)

    ctor.parameters.forEach { param ->
        val paramType: TypeName = param.type
        val propBuilder = PropertySpec.builder(param.name, paramType)
            .initializer(param.name)

        val simpleName = (paramType as? ClassName)?.simpleName
        if (simpleName != null && simpleName in wrapperNames) {
            propBuilder.addAnnotation(
                AnnotationSpec.builder(Contextual::class)
                    .useSiteTarget(AnnotationSpec.UseSiteTarget.PROPERTY)
                    .build()
            )
        }

        builder.addProperty(propBuilder.build())
    }

    return builder.build()
}

private fun generateRequestClass(
    model: ModelSpec,
    wrappers: List<InlineWrapper>,
    suffix: String,
    filter: (PropertySpecData) -> Boolean,
    makeNullable: Boolean
): TypeSpec {
    val reqProps = model.props.filter(filter)
    val wrapperNames = wrappers.map { it.name }.toSet()

    val ctor = FunSpec.constructorBuilder().apply {
        reqProps.forEach { p ->
            val t = if (makeNullable) p.type.copy(nullable = true) else p.type
            addParameter(p.name, t)
        }
    }.build()

    val builder = TypeSpec.classBuilder("${model.name}$suffix")
        .addModifiers(KModifier.DATA)
        .addAnnotation(Serializable::class)
        .primaryConstructor(ctor)

    ctor.parameters.forEach { param ->
        val paramType: TypeName = param.type
        val propBuilder = PropertySpec.builder(param.name, paramType)
            .initializer(param.name)

        val simpleName = (paramType as? ClassName)?.simpleName
        if (simpleName != null && simpleName in wrapperNames) {
            propBuilder.addAnnotation(
                AnnotationSpec.builder(Contextual::class)
                    .useSiteTarget(AnnotationSpec.UseSiteTarget.PROPERTY)
                    .build()
            )
        }
        builder.addProperty(propBuilder.build())
    }

    return builder.build()
}

fun generateCreateClass(model: ModelSpec, wrappers: List<InlineWrapper>): TypeSpec =
    generateRequestClass(model, wrappers, "Create", { it.inCreate }, false)

fun generatePatchClass(model: ModelSpec, wrappers: List<InlineWrapper>): TypeSpec =
    generateRequestClass(model, wrappers, "Patch", { it.inPatch }, true)
