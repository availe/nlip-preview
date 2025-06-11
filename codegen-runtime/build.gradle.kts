plugins {
    alias(libs.plugins.kotlinJvm)
}

group = "io.availe"
version = "1.0.0"

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(projects.codegen)
    implementation(projects.modelKspProcessor)
    implementation(libs.logback)
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.metadata)
    implementation(libs.kotlinpoet.metadata.specs)
    implementation(libs.kotlinpoet.ksp)
}
