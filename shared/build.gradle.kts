

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

interface KspDependencies { fun ksp(dep: Any) }

fun KotlinTarget.kspDependencies(block: KspDependencies.() -> Unit) {
    val configurationName = "ksp${targetName.replaceFirstChar { it.uppercaseChar() }}"
    project.dependencies {
        object : KspDependencies { override fun ksp(dep: Any) { add(configurationName, dep) } }.block()
    }
}

fun KotlinMultiplatformExtension.kspDependenciesForAllTargets(block: KspDependencies.() -> Unit) {
    targets.configureEach { if (targetName != "metadata") kspDependencies(block) }
}

fun KotlinMultiplatformExtension.commonMainKspDependencies(
    project: Project,
    block: KspDependencies.() -> Unit
) {
    project.dependencies {
        object : KspDependencies { override fun ksp(dep: Any) { add("kspCommonMainMetadata", dep) } }.block()
    }

    sourceSets.named("commonMain").configure {
        kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
    }

    project.tasks.withType(KotlinCompilationTask::class.java).configureEach {
        if (name != "kspCommonMainKotlinMetadata") dependsOn("kspCommonMainKotlinMetadata")
    }
}


@Suppress("UNCHECKED_CAST")
val secrets = rootProject.extra["secrets"] as Map<String, String>

fun requireSecret(key: String): String =
    secrets[key] ?: error("Missing required secret: $key")

val baseUrl = requireSecret("BASE_URL")
val devBaseUrl = requireSecret("DEV_BASE_URL")

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.buildKonfig)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinx.rpc.plugin)
}

buildkonfig {
    packageName = "io.availe.config"
    exposeObjectWithName = "SharedBuildConfig"
    defaultConfigs {
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "BASE_URL",
            baseUrl
        )
    }
    defaultConfigs("dev") {
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "BASE_URL",
            devBaseUrl
        )
    }
}

kotlin {
    jvmToolchain(21)
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    jvm()
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        add(rootProject.path)
                        add(projectDir.path)
                    }
                }
            }
        }
    }
    sourceSets {
        commonMain {
            kotlin.srcDir(layout.buildDirectory.dir("generated-src/kotlin-poet"))
            dependencies {
                implementation(project(":model-ksp-annotations"))
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.cio)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.arrow.core)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.rpc.core)
                implementation(libs.kotlinx.rpc.krpc.client)
                implementation(libs.kotlinx.rpc.krpc.serialization.json)
                implementation(libs.kotlinx.rpc.krpc.ktor.client)
            }
        }
        jvmMain {
            dependencies {
                implementation(libs.ktor.client.cio)
            }
        }
        wasmJsMain {
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }
        iosMain {
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
    }

    commonMainKspDependencies(project) {
        ksp(project(":model-ksp-processor"))
    }
}

android {
    namespace = "io.availe.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

tasks.withType<KotlinCompile>().configureEach {
    dependsOn(tasks.named("generateBuildKonfig"))
}