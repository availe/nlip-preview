package io.availe.definitions

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
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
