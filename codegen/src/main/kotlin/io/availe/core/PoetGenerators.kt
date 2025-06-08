@file:OptIn(ExperimentalUuidApi::class)

package io.availe.core

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private val optionClass = ClassName("arrow.core", "Option")

private fun optionOf(t: TypeName): TypeName = optionClass.parameterizedBy(t)

private fun wrapForPatch(original: TypeName): TypeName =
    if (original is ParameterizedTypeName && original.rawType == optionClass) original
    else optionOf(original)

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

private fun annotateContextualIfNeeded(
    propBuilder: PropertySpec.Builder,
    type: TypeName,
    wrapperNames: Set<String>
) {
    val isOption = type is ParameterizedTypeName && type.rawType == optionClass
    val targetType = when (type) {
        is ParameterizedTypeName ->
            if (type.rawType == optionClass) type.typeArguments.first() else null

        is ClassName -> type
        else -> null
    }
    if (isOption ||
        (targetType is ClassName && targetType.simpleName in wrapperNames)
    ) {
        propBuilder.addAnnotation(
            AnnotationSpec.builder(Contextual::class)
                .useSiteTarget(AnnotationSpec.UseSiteTarget.PROPERTY)
                .build()
        )
    }
}

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
        val propBuilder = PropertySpec.builder(param.name, param.type)
            .initializer(param.name)
        annotateContextualIfNeeded(propBuilder, param.type, wrapperNames)
        builder.addProperty(propBuilder.build())
    }
    return builder.build()
}

private fun generateRequestClass(
    model: ModelSpec,
    wrappers: List<InlineWrapper>,
    suffix: String,
    filter: (PropertySpecData) -> Boolean,
    wrapOptional: Boolean
): TypeSpec {
    val reqProps = model.props.filter(filter)
    val wrapperNames = wrappers.map { it.name }.toSet()
    val ctor = FunSpec.constructorBuilder().apply {
        reqProps.forEach { p ->
            val t = if (wrapOptional) wrapForPatch(p.type) else p.type
            addParameter(p.name, t)
        }
    }.build()
    val builder = TypeSpec.classBuilder("${model.name}$suffix")
        .addModifiers(KModifier.DATA)
        .addAnnotation(Serializable::class)
        .primaryConstructor(ctor)
    ctor.parameters.forEach { param ->
        val propBuilder = PropertySpec.builder(param.name, param.type)
            .initializer(param.name)
        annotateContextualIfNeeded(propBuilder, param.type, wrapperNames)
        builder.addProperty(propBuilder.build())
    }
    return builder.build()
}

fun generateCreateClass(model: ModelSpec, wrappers: List<InlineWrapper>): TypeSpec =
    generateRequestClass(model, wrappers, "Create", { it.inCreate }, false)

fun generatePatchClass(model: ModelSpec, wrappers: List<InlineWrapper>): TypeSpec =
    generateRequestClass(model, wrappers, "Patch", { it.inPatch }, true)
