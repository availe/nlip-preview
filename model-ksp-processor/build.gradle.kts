plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ksp)
    application
}

group = "io.availe"
version = "1.0.0"

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(mapOf("path" to ":codegen", "configuration" to "jvmRuntimeElements")))
    implementation(project(mapOf("path" to ":model-ksp-annotations", "configuration" to "jvmRuntimeElements")))

    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.metadata)
    implementation(libs.kotlinpoet.metadata.specs)
    implementation(libs.kotlinpoet.ksp)
    implementation(libs.ksp.symbol.processing.api)

    implementation(libs.logback)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.arrow.core)
}

application {
    mainClass.set("io.availe.ApplicationKt")
}
