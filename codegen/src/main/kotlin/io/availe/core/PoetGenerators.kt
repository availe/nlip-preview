package io.availe.core

import com.squareup.kotlinpoet.*

fun generateValueClass(wrapper: InlineWrapper): TypeSpec {
    return TypeSpec.classBuilder(wrapper.name)
        .addModifiers(KModifier.VALUE)
        .addAnnotation(JvmInline::class)
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
}

fun generateDataClass(model: ModelSpec): TypeSpec {
    val ctor = FunSpec.constructorBuilder().apply {
        model.props.forEach {
            addParameter(it.name, it.type)
        }
    }.build()
    val klass = TypeSpec.classBuilder(model.name)
        .addModifiers(KModifier.DATA)
        .primaryConstructor(ctor)
    ctor.parameters.forEach {
        klass.addProperty(
            PropertySpec.builder(it.name, it.type)
                .initializer(it.name)
                .build()
        )
    }
    return klass.build()
}
