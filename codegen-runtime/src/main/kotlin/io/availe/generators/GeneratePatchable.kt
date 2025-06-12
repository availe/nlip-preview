package io.availe.generators

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.File

private const val pkg = "io.availe.models"

fun generatePatchable() {
    val out: File = filePath
    val t = TypeVariableName("T", KModifier.OUT)
    val unchanged = TypeSpec.objectBuilder("Unchanged")
        .addSuperinterface(ClassName(pkg, "Patchable").parameterizedBy(ClassName("kotlin", "Nothing")))
        .addAnnotation(ClassName("kotlinx.serialization", "Serializable"))
        .build()
    val setCls = TypeSpec.classBuilder("Set")
        .addModifiers(KModifier.DATA)
        .addTypeVariable(TypeVariableName("T"))
        .primaryConstructor(
            FunSpec.constructorBuilder().addParameter("value", TypeVariableName("T")).build()
        )
        .addProperty(PropertySpec.builder("value", TypeVariableName("T")).initializer("value").build())
        .addSuperinterface(ClassName(pkg, "Patchable").parameterizedBy(TypeVariableName("T")))
        .addAnnotation(ClassName("kotlinx.serialization", "Serializable"))
        .build()
    val patchable = TypeSpec.classBuilder("Patchable")
        .addTypeVariable(t)
        .addModifiers(KModifier.SEALED)
        .addAnnotation(ClassName("kotlinx.serialization", "Serializable"))
        .addType(unchanged)
        .addType(setCls)
        .build()
    FileSpec.builder(pkg, "Patchable").addType(patchable).build().writeTo(out)
}
