@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

interface KspDependencies {
    fun ksp(dep: Any)
}

fun KotlinTarget.kspDependencies(block: KspDependencies.() -> Unit) {
    val configurationName = "ksp${targetName.replaceFirstChar { it.uppercaseChar() }}"
    project.dependencies {
        object : KspDependencies {
            override fun ksp(dep: Any) {
                add(configurationName, dep)
            }
        }.block()
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
        add("kspCommonMainMetadata", project(":model-ksp-processor"))
        add("kspCommonMainMetadata", project(":model-ksp-annotations"))
        add("kspCommonMainMetadata", project(":codegen"))
    }

    project.dependencies {
        object : KspDependencies {
            override fun ksp(dep: Any) {
                add("kspCommonMainMetadata", dep)
            }
        }.block()
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
        nodejs()
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
                implementation(projects.codegen)
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

val codegen by configurations.creating

dependencies {
    codegen(project(":codegen-runtime"))
}

tasks.register<JavaExec>("runCodegen") {
    group = "build"
    description = "Runs the KSP-based code generator"

    mainClass.set("io.availe.ApplicationKt")
    classpath = codegen

    val modelsJsonFile = layout.buildDirectory.file("generated/ksp/metadata/commonMain/resources/models.json")

    args(modelsJsonFile.get().asFile.absolutePath, "--generate-patchable")
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    if (name.startsWith("compile")) {
        dependsOn(tasks.named("runCodegen"))
    }
}

kotlin.sourceSets.named("commonMain") {
    kotlin.srcDir(layout.buildDirectory.dir("generated/kotlin-poet"))
}

val modelJsonElements by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, "model-definition"))
    }
}

tasks.matching { it.name == "kspCommonMainKotlinMetadata" }.all {
    outputs.files.asFileTree.forEach { file ->
        if (file.name == "models.json") {
            artifacts {
                add(modelJsonElements.name, file) {
                    builtBy(tasks.named("kspCommonMainKotlinMetadata"))
                }
            }
        }
    }
}