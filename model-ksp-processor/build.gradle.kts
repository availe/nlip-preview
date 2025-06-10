plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.ksp)
    application
}


kotlin {
    jvmToolchain(21)

    sourceSets {
        commonMain.dependencies {
            implementation(projects.codegen)

            implementation(libs.logback)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.arrow.core)

            implementation(libs.kotlinpoet)
            implementation(libs.kotlinpoet.metadata)
            implementation(libs.kotlinpoet.metadata.specs)
            implementation(libs.kotlinpoet.ksp)
            implementation(libs.ksp.symbol.processing.api)
        }
    }
}

group = "io.availe"
version = "1.0.0"
application {
    mainClass.set("io.availe.ApplicationKt")
}
