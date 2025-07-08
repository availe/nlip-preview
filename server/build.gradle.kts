import nu.studer.gradle.jooq.JooqEdition
import org.jooq.meta.jaxb.Logging

/** ------------- Load plugins ------------- */

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.jooq)
    alias(libs.plugins.flyway)
    alias(libs.plugins.shadow)
    id("org.jetbrains.kotlin.kapt")
    application
}

kotlin {
    jvmToolchain(21)
}

group = "io.availe"
version = "1.0.0"
application {
    mainClass.set("io.availe.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.server.core)

    jooqGenerator(libs.postgresql)

    implementation(libs.mapstruct)
    kapt(libs.mapstruct.processor)

    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.logback)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.arrow.core)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.ktor.server.cors.jvm)
    implementation(libs.ktor.server.status.pages.jvm)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.postgresql)
    testImplementation(libs.flyway.core)
    testImplementation(libs.flyway.database.postgresql)
    testImplementation(libs.snakeyaml)
    testImplementation(platform(libs.jackson.bom))
    testImplementation(libs.jackson.databind)
}

// produce bundled jvm app for docker
tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveBaseName.set("server")
    archiveClassifier.set("all")
    archiveVersion.set("")
    mergeServiceFiles()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    dependsOn(project(":shared").tasks.named("openApiGenerate"))
}
