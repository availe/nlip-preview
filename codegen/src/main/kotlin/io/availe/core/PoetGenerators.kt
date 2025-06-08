package io.availe.core

import com.squareup.kotlinpoet.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

fun generateValueClass(wrapper: InlineWrapper): TypeSpec =
    TypeSpec.classBuilder(wrapper.name)
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
        val typeName = param.type
        val propBuilder = PropertySpec.builder(param.name, typeName)
            .initializer(param.name)

        if (typeName is ClassName && typeName.simpleName in wrapperNames) {
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
