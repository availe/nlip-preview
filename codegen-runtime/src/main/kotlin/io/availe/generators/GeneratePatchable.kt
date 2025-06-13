package io.availe.generators

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.availe.*

fun generatePatchable() {
    val typeVariable = TypeVariableName("T", KModifier.OUT)
    val serializableAnnotation = ClassName(
        SERIALIZABLE_QUALIFIED_NAME.substringBeforeLast('.'),
        SERIALIZABLE_QUALIFIED_NAME.substringAfterLast('.')
    )

    val unchangedObject = TypeSpec.objectBuilder(UNCHANGED_OBJECT_NAME)
        .superclass(
            ClassName(MODELS_PACKAGE_NAME, PATCHABLE_CLASS_NAME).parameterizedBy(
                ClassName(
                    "kotlin",
                    "Nothing"
                )
            )
        )
        .addAnnotation(serializableAnnotation)
        .build()

    val setClass = TypeSpec.classBuilder(SET_CLASS_NAME)
        .addModifiers(KModifier.DATA)
        .addTypeVariable(TypeVariableName("T"))
        .primaryConstructor(
            FunSpec.constructorBuilder().addParameter(VALUE_PROPERTY_NAME, TypeVariableName("T")).build()
        )
        .addProperty(
            PropertySpec.builder(VALUE_PROPERTY_NAME, TypeVariableName("T")).initializer(VALUE_PROPERTY_NAME).build()
        )
        .superclass(ClassName(MODELS_PACKAGE_NAME, PATCHABLE_CLASS_NAME).parameterizedBy(TypeVariableName("T")))
        .addAnnotation(serializableAnnotation)
        .build()

    val patchableClass = TypeSpec.classBuilder(PATCHABLE_CLASS_NAME)
        .addTypeVariable(typeVariable)
        .addModifiers(KModifier.SEALED)
        .addAnnotation(serializableAnnotation)
        .addType(unchangedObject)
        .addType(setClass)
        .build()

    FileSpec.builder(MODELS_PACKAGE_NAME, PATCHABLE_CLASS_NAME).addType(patchableClass).build()
        .writeTo(OUTPUT_DIRECTORY)
}