package io.availe.definitions

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import io.availe.core.*
import java.io.File

object Paths {
    val sharedRoot = File("../shared/build/generated-src/kotlin-poet")
    val serverRoot = File("../server/build/generated-src/kotlin-poet")
}

fun writeShared(fileName: String, vararg types: TypeSpec) {
    FileSpec.builder("io.availe.models", fileName)
        .apply { types.forEach { addType(it) } }
        .build()
        .writeTo(Paths.sharedRoot)
}

fun writeServer(fileName: String, vararg types: TypeSpec) {
    FileSpec.builder("io.availe.models", fileName)
        .apply { types.forEach { addType(it) } }
        .build()
        .writeTo(Paths.serverRoot)
}

private fun buildMainType(
    model: io.availe.core.ModelSpec,
    spec: CodegenBuilder,
    includeNestedEnums: Boolean
): TypeSpec {
    val builder = generateDataClass(model, spec.wrappers).toBuilder()
    if (includeNestedEnums) {
        spec.enums
            .filter { it.nestedIn == model.name }
            .forEach { builder.addType(generateEnum(it)) }
    }
    return builder.build()
}

fun writeSharedModels(spec: CodegenBuilder, includeNestedEnums: Boolean = false) {
    spec.models.forEach { model ->
        val main = buildMainType(model, spec, includeNestedEnums)
        val create = generateCreateClass(model, spec.wrappers)
        val patch = generatePatchClass(model, spec.wrappers)
        writeShared(model.name, main, create, patch)
    }
}

fun writeServerModels(
    spec: CodegenBuilder,
    includeNestedEnums: Boolean = false,
    onlyMain: Boolean = false
) {
    spec.models.forEach { model ->
        val main = buildMainType(model, spec, includeNestedEnums)
        if (onlyMain) {
            writeServer(model.name, main)
        } else {
            val create = generateCreateClass(model, spec.wrappers)
            val patch = generatePatchClass(model, spec.wrappers)
            writeServer(model.name, main, create, patch)
        }
    }
}
