// file: model-ksp-annotations/build.gradle.kts

plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

group = "io.availe"
version = "1.0.0"

kotlin {
    // only a JVM target + the metadata target
    jvm()

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
    }
}
