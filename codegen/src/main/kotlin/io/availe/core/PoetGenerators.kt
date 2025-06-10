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

// collect every model name as we generate its data class
private val allModelNames = mutableSetOf<String>()

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
    // register for nested‐model lookup
    allModelNames += model.name

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

sealed class RequestSuffix(val suffix: String) {
    object Create : RequestSuffix("Create")
    object Patch : RequestSuffix("Patch")
}

private fun remapNestedProperty(
    prop: PropertySpecData,
    suffix: RequestSuffix,
    wrapOptionals: Boolean
): Pair<String, TypeName> {
    val original = prop.type
    val innerClass = when (original) {
        is ParameterizedTypeName ->
            if (original.rawType == optionClass && original.typeArguments.first() is ClassName)
                original.typeArguments.first() as ClassName
            else null

        is ClassName -> original
        else -> null
    }
    if (innerClass != null && innerClass.simpleName in allModelNames) {
        val base = innerClass.simpleName
        val pkg = innerClass.packageName
        val newType = ClassName(pkg, "${base}${suffix.suffix}")
        val name = "${prop.name}${suffix.suffix}"
        val type = if (suffix is RequestSuffix.Patch && wrapOptionals) optionOf(newType) else newType
        return name to type
    }
    val name = prop.name
    val type = if (wrapOptionals) wrapForPatch(original) else original
    return name to type
}

private fun generateRequestClass(
    model: ModelSpec,
    wrappers: List<InlineWrapper>,
    suffix: RequestSuffix,
    filter: (PropertySpecData) -> Boolean,
    wrapOptional: Boolean
): TypeSpec {
    val reqProps = model.props.filter(filter)
    val wrapperNames = wrappers.map { it.name }.toSet()
    val ctorBuilder = FunSpec.constructorBuilder()
    val nameTypePairs = reqProps.map { prop ->
        val (name, type) = remapNestedProperty(prop, suffix, wrapOptional)
        ctorBuilder.addParameter(name, type)
        name to type
    }
    val ctor = ctorBuilder.build()
    val builder = TypeSpec.classBuilder("${model.name}${suffix.suffix}")
        .addModifiers(KModifier.DATA)
        .addAnnotation(Serializable::class)
        .primaryConstructor(ctor)
    nameTypePairs.forEach { (name, type) ->
        val propBuilder = PropertySpec.builder(name, type)
            .initializer(name)
        annotateContextualIfNeeded(propBuilder, type, wrapperNames)
        builder.addProperty(propBuilder.build())
    }
    return builder.build()
}

fun generateCreateClass(model: ModelSpec, wrappers: List<InlineWrapper>): TypeSpec =
    generateRequestClass(model, wrappers, RequestSuffix.Create, { it.inCreate }, false)

fun generatePatchClass(model: ModelSpec, wrappers: List<InlineWrapper>): TypeSpec =
    generateRequestClass(model, wrappers, RequestSuffix.Patch, { it.inPatch }, true)
