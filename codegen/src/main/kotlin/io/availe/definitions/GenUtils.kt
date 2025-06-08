package io.availe.definitions

import com.squareup.kotlinpoet.FileSpec
import java.io.File

object Paths {
    val sharedRoot = File("../shared/build/generated-src/kotlin-poet")
    val serverRoot = File("../server/build/generated-src/kotlin-poet")
}

fun writeShared(name: String, type: com.squareup.kotlinpoet.TypeSpec) {
    FileSpec.builder("io.availe.models", name)
        .addType(type)
        .build()
        .writeTo(Paths.sharedRoot)
}

fun writeServer(name: String, type: com.squareup.kotlinpoet.TypeSpec) {
    FileSpec.builder("io.availe.models", name)
        .addType(type)
        .build()
        .writeTo(Paths.serverRoot)
}
