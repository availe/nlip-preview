plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

group = "io.availe"
version = "1.0.0"

kotlin {
    jvmToolchain(21)
    jvm()

    sourceSets {
        commonMain {
            kotlin.srcDir("src/main/kotlin")
            dependencies {
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)
                implementation(libs.arrow.core)
            }
        }

        jvmMain {
            dependencies {
                implementation(libs.logback)
                implementation(libs.kotlinpoet)
                implementation(libs.kotlinpoet.metadata)
                implementation(libs.kotlinpoet.metadata.specs)
                implementation(libs.kotlinpoet.ksp)
            }
        }
    }
}
