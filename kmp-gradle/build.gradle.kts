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

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
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
                implementation(projects.codegen)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.arrow.core)
                implementation(libs.kotlinx.datetime)
            }
        }
    }

    commonMainKspDependencies(project) {
        ksp(project(":model-ksp-processor"))
    }
}

android {
    namespace = "io.availe.kmpgradle"
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
    // Add the codegen-runtime project to our new configuration
    codegen(project(":codegen-runtime"))
}

// The task that will execute our code generator
tasks.register<JavaExec>("runCodegen") {
    group = "build"
    description = "Runs the KSP-based code generator"
    // This task only needs to run after the KSP task creates the json file
    dependsOn(tasks.named("kspCommonMainKotlinMetadata"))

    mainClass.set("io.availe.ApplicationKt")
    classpath = codegen

    // Define the path to the generated json file
    val modelsJsonFile = layout.buildDirectory.file("generated/ksp/metadata/commonMain/resources/models.json")

    // Pass the absolute path of the file to the main function as an argument
    args(modelsJsonFile.get().asFile.absolutePath, "--generate-patchable")
}

// All compilation tasks must wait for the codegen to finish generating sources
tasks.withType<KotlinCompilationTask<*>>().configureEach {
    if (name.startsWith("compile")) {
        dependsOn(tasks.named("runCodegen"))
    }
}

kotlin.sourceSets.named("commonMain") {
    kotlin.srcDir(layout.buildDirectory.dir("generated/kotlin-poet"))
}

