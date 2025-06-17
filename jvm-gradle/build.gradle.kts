import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask


buildscript {
    repositories { mavenCentral() }
}

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ksp)
    application
}

kotlin {
    jvmToolchain(21)

    sourceSets {
        named("main") {
            kotlin.srcDir(
                layout.buildDirectory.dir("generated-src/kotlin-poet")
            )
        }
    }
}

group = "io.availe"
version = "1.0.0"
application {
    mainClass.set("io.availe.ApplicationKt")
}

dependencies {
    ksp(project(":model-ksp-processor"))
    ksp(project(":model-ksp-annotations"))
    ksp(project(":codegen"))
    implementation(projects.codegen)
    implementation(project(":model-ksp-annotations"))
    implementation(projects.kmpGradle)
    implementation(libs.logback)
}

val codegen by configurations.creating

dependencies {
    // Add the codegen-runtime project to our new configuration
    codegen(project(":codegen-runtime"))
}

tasks.register<JavaExec>("runCodegen") {
    group = "build"
    description = "Runs the KSP-based code generator"
    dependsOn(tasks.named("kspKotlin"), project(":shared").tasks.named("kspCommonMainKotlinMetadata"))

    mainClass.set("io.availe.ApplicationKt")
    classpath = codegen

    val serverJsonFile = layout.buildDirectory.file("generated/ksp/main/resources/models.json")
    val sharedJsonFile =
        project(":shared").layout.buildDirectory.file("generated/ksp/metadata/commonMain/resources/models.json")

    args(serverJsonFile.get().asFile.absolutePath, sharedJsonFile.get().asFile.absolutePath)
}

// All compilation tasks must wait for the codegen to finish generating sources
tasks.withType<KotlinCompilationTask<*>>().configureEach {
    if (name == "compileKotlin") {
        dependsOn(tasks.named("runCodegen"))
    }
}

kotlin.sourceSets.named("main") {
    kotlin.srcDir(layout.buildDirectory.dir("generated/kotlin-poet"))
}

