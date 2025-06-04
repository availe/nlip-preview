import nu.studer.gradle.jooq.JooqEdition
import org.jooq.meta.jaxb.Logging

/** ------------- Load .env ------------- */

@Suppress("UNCHECKED_CAST")
val secrets = rootProject.extra["secrets"] as Map<String, String>

fun requireSecret(key: String): String =
    secrets[key] ?: error("Missing required secret: $key")

val dbUrl = requireSecret("DB_URL")
val dbUser = requireSecret("DB_USER")
val dbPass = requireSecret("DB_PASS")
val dbSchema = requireSecret("DB_SCHEMA")

tasks.withType<Test>().configureEach {
    environment("DB_URL", dbUrl)
    environment("DB_USER", dbUser)
    environment("DB_PASS", dbPass)
    environment("DB_SCHEMA", dbSchema)
}

/** ------------- Buildscript class-path for Flyway ------------- */
buildscript {
    repositories { mavenCentral() }
    dependencies {
        classpath(libs.flyway.database.postgresql)
        classpath(libs.postgresql)
    }
}

/** ------------- Load plugins ------------- */

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.jooq)
    alias(libs.plugins.flyway)
    alias(libs.plugins.shadow)
    alias(libs.plugins.kotlinx.rpc.plugin)
    id("org.jetbrains.kotlin.kapt")
    application
}

kotlin {
    jvmToolchain(21)
}

/** ------------- Add a dedicated source set for jOOQ strategy ------------- */
sourceSets {
    create("jooq") {
        kotlin.srcDir("src/jooq/kotlin")
    }
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

    implementation(libs.mapstruct)
    kapt(libs.mapstruct.processor)

    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.arrow.core)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.ktor.server.cors.jvm)
    implementation(libs.ktor.server.status.pages.jvm)

    implementation(libs.kotlinx.rpc.krpc.server)
    implementation(libs.kotlinx.rpc.krpc.ktor.server)
    implementation(libs.kotlinx.rpc.krpc.serialization.json)

    implementation(libs.jooq)
    compileOnly(libs.jooq.codegen)
    compileOnly(libs.jooq.meta)

    // jOOQ codegen APIs for the "jooq" source set
    add("jooqImplementation", libs.jooq.meta)
    add("jooqImplementation", libs.jooq.codegen)

    // JDBC driver for codegen and our custom strategy class
    jooqGenerator(libs.postgresql)
    jooqGenerator(sourceSets["jooq"].output)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.postgresql)
    testImplementation(libs.flyway.core)
    testImplementation(libs.flyway.database.postgresql)
    testImplementation(libs.snakeyaml)
    testImplementation(platform(libs.jackson.bom))
    testImplementation(libs.jackson.databind)
}

/** ------------- jOOQ config ------------- */

jooq {
    edition.set(JooqEdition.OSS)
    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(true)

            jooqConfiguration.apply {
                logging = Logging.WARN

                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = dbUrl
                    user = dbUser
                    password = dbPass
                }

                generator.apply {
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = dbSchema
                    }
                    generate.apply {
                        isImmutablePojos = true
                        isRecords = true
                    }
                    target.apply {
                        packageName = "io.availe.jooq"
                        directory = layout.buildDirectory
                            .dir("generated-src/jooq/main")
                            .get()
                            .asFile
                            .absolutePath
                    }
                }
            }
        }
    }
}

sourceSets["main"].java.srcDir(
    layout.buildDirectory.dir("generated-src/jooq/main")
)

/** ------------- Flyway config ------------- */

flyway {
    url = dbUrl
    user = dbUser
    password = dbPass
    schemas = arrayOf(dbSchema)
    locations = arrayOf("filesystem:${rootProject.layout.projectDirectory.asFile}/src/main/resources/db/migration")
    cleanDisabled = false
}

/** ------------- Build hooks & helper tasks ------------- */

// Always run Flyway before compiling Kotlin and generating jOOQ classes
tasks.named("compileKotlin") {
    dependsOn("flywayMigrate")
}

// Always run Flyway before starting the server
tasks.named<JavaExec>("run") {
    dependsOn("flywayMigrate")
}

// Drop all objects from DB
tasks.register("clearDb") {
    group = "database"
    description = "Drops all objects"
    dependsOn("flywayClean")
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
