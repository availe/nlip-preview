package io.availe.core

import com.squareup.kotlinpoet.*
import kotlinx.serialization.Serializable

fun generateValueClass(wrapper: InlineWrapper): TypeSpec =
    TypeSpec.classBuilder(wrapper.name)
        .addModifiers(KModifier.VALUE)
        .addAnnotation(JvmInline::class)
        .addAnnotation(Serializable::class)
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter("value", wrapper.backing.asTypeName())
                .build()
        )
        .addProperty(
            PropertySpec.builder("value", wrapper.backing.asTypeName())
                .initializer("value")
                .build()
        )
        .build()

fun generateEnum(spec: EnumSpec): TypeSpec =
    TypeSpec.enumBuilder(spec.name)
        .addAnnotation(Serializable::class)
        .apply { spec.values.forEach { addEnumConstant(it.uppercase()) } }
        .build()

fun generateDataClass(model: ModelSpec): TypeSpec {
    val ctor = FunSpec.constructorBuilder().apply {
        model.props.forEach { addParameter(it.name, it.type) }
    }.build()
    return TypeSpec.classBuilder(model.name)
        .addModifiers(KModifier.DATA)
        .addAnnotation(Serializable::class)
        .primaryConstructor(ctor)
        .apply {
            ctor.parameters.forEach {
                addProperty(
                    PropertySpec.builder(it.name, it.type)
                        .initializer(it.name)
                        .build()
                )
            }
        }
        .build()
}
