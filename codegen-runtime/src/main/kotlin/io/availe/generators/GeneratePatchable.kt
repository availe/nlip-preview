package io.availe.generators

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.File

fun generatePatchable() {
    val outputDirectory: File = File("build/generated-src/kotlin-poet")
    val packageName = "io.availe.models"
    val typeVariable = TypeVariableName("T", KModifier.OUT)

    val unchangedObject = TypeSpec.objectBuilder("Unchanged")
        .superclass(ClassName(packageName, "Patchable").parameterizedBy(ClassName("kotlin", "Nothing")))
        .addAnnotation(ClassName("kotlinx.serialization", "Serializable"))
        .build()

    val setClass = TypeSpec.classBuilder("Set")
        .addModifiers(KModifier.DATA)
        .addTypeVariable(TypeVariableName("T"))
        .primaryConstructor(
            FunSpec.constructorBuilder().addParameter("value", TypeVariableName("T")).build()
        )
        .addProperty(PropertySpec.builder("value", TypeVariableName("T")).initializer("value").build())
        .superclass(ClassName(packageName, "Patchable").parameterizedBy(TypeVariableName("T")))
        .addAnnotation(ClassName("kotlinx.serialization", "Serializable"))
        .build()

    val patchableClass = TypeSpec.classBuilder("Patchable")
        .addTypeVariable(typeVariable)
        .addModifiers(KModifier.SEALED)
        .addAnnotation(ClassName("kotlinx.serialization", "Serializable"))
        .addType(unchangedObject)
        .addType(setClass)
        .build()

    FileSpec.builder(packageName, "Patchable").addType(patchableClass).build().writeTo(outputDirectory)
}