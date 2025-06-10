import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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

dependencies {
    ksp(project(":model-ksp-processor"))
    implementation(project(":model-ksp-annotations"))
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
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(
                layout.buildDirectory
                    .dir("generated-src/kotlin-poet")
            )
            dependencies {
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
        jvmMain.dependencies {
            implementation(libs.ktor.client.cio)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    namespace = "io.availe.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

tasks.withType<KotlinCompile>().configureEach {
    dependsOn(tasks.named("generateModels"))
    dependsOn(tasks.named("generateBuildKonfig"))
}

val generateModels by tasks.registering(JavaExec::class) {
    group = "codegen"
    description = "Run the KotlinPoet code generator"
    classpath = project(":codegen")
        .sourceSets["main"].runtimeClasspath
    mainClass.set("io.availe.ApplicationKt")
    workingDir = projectDir
}
