@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

group = "io.availe"
version = "1.0.0"

kotlin {
    jvmToolchain(21)
    jvm()
    iosX64(); iosArm64(); iosSimulatorArm64()
    wasmJs(); macosX64(); macosArm64(); linuxX64()
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)
                implementation(libs.arrow.core)
            }
        }
    }
}