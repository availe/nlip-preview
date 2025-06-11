// file: model-ksp-annotations/build.gradle.kts

plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

group = "io.availe"
version = "1.0.0"

kotlin {
    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    wasmJs()
    linuxX64()
    macosX64()
    macosArm64()

    sourceSets {
        val commonMain by getting {
            // annotation definitions live here
            kotlin.srcDir("src/main/kotlin")
            dependencies {
                // common‐world users get the "runtimeElements" (metadata) variant
                implementation(project(mapOf("path" to ":codegen", "configuration" to "jvmRuntimeElements")))
            }
        }
        val jvmMain by getting {
            dependencies {
                // JVM users (and KSP processors) get the JVM‐specific artifact
                implementation(project(
                    mapOf(
                        "path" to ":codegen",
                        "configuration" to "jvmRuntimeElements"
                    )
                ))
            }
        }

        val iosX64Main by getting

        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val wasmJsMain by getting
        val linuxX64Main by getting
        val macosX64Main by getting
        val macosArm64Main by getting
    }
}
