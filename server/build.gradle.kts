import nu.studer.gradle.jooq.JooqEdition
import org.jooq.meta.jaxb.Logging

@Suppress("UNCHECKED_CAST")
val secrets = rootProject.extra["secrets"] as Map<String, String>

fun requireSecret(key: String): String =
    secrets[key] ?: error("Missing required secret: $key")

val dbHost = requireSecret("DB_HOST")
val dbPort = requireSecret("DB_PORT")
val dbName = requireSecret("DB_NAME")
val dbUser = requireSecret("DB_USER")
val dbPass = requireSecret("DB_PASS")
val dbSchema = requireSecret("DB_SCHEMA")

val dbUrl = "jdbc:postgresql://$dbHost:$dbPort/$dbName?currentSchema=$dbSchema"

val dbEnv = mapOf(
    "DB_HOST" to dbHost,
    "DB_PORT" to dbPort,
    "DB_NAME" to dbName,
    "DB_USER" to dbUser,
    "DB_PASS" to dbPass,
    "DB_SCHEMA" to dbSchema,
    "DB_URL" to dbUrl
)

buildscript {
    repositories { mavenCentral() }
    dependencies {
        classpath(libs.flyway.database.postgresql)
        classpath(libs.postgresql)
    }
}

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

sourceSets {
    create("jooq") {
        kotlin.srcDir("src/jooq/kotlin")
    }
}

kotlin {
    jvmToolchain(21)

    sourceSets {
        named("main") {
            kotlin.srcDir(
                layout.buildDirectory.dir("generated-src/kotlin-poet")
            )
        }
//        create("jooq") {
//            kotlin.srcDir("src/jooq/kotlin")
//        }
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
    implementation(libs.argon2.jvm)
    implementation(libs.kotlinx.datetime)
    implementation(libs.hikaricp)
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)
    implementation(libs.postgresql)
    implementation(libs.dotenv.kotlin)

    implementation(libs.kotlinx.rpc.krpc.server)
    implementation(libs.kotlinx.rpc.krpc.ktor.server)
    implementation(libs.kotlinx.rpc.krpc.serialization.json)

    implementation(libs.jooq)
    compileOnly(libs.jooq.codegen)
    compileOnly(libs.jooq.meta)

    add("jooqImplementation", libs.jooq.meta)
    add("jooqImplementation", libs.jooq.codegen)

    jooqGenerator(kotlin("stdlib"))
    jooqGenerator(sourceSets["jooq"].output)
    jooqGenerator(libs.postgresql)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.snakeyaml)
    testImplementation(platform(libs.jackson.bom))
    testImplementation(libs.jackson.databind)
    testImplementation(libs.testcontainers.postgresql)
    testRuntimeOnly(libs.postgresql)

    testImplementation(platform("org.junit:junit-bom:5.10.2")) // Or whatever is the latest
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

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
                    name = "org.jooq.codegen.KotlinGenerator"
                    strategy.apply {
                        name = "io.availe.jooq.CustomGeneratorStrategy"
                    }
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = dbSchema
                    }
                    generate.apply {
                        isImmutablePojos = true
                        isRecords = true
                        isImplicitJoinPathsAsKotlinProperties = true
                        isKotlinSetterJvmNameAnnotationsOnIsPrefix = true
                        isPojosAsKotlinDataClasses = true
                        isKotlinNotNullPojoAttributes = true
                        isKotlinNotNullRecordAttributes = true
                        isKotlinNotNullInterfaceAttributes = true
                        isKotlinDefaultedNullablePojoAttributes = false
                        isKotlinDefaultedNullableRecordAttributes = false
                        isDaos = true
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

flyway {
    url = dbUrl
    user = dbUser
    password = dbPass
    schemas = arrayOf(dbSchema)
    locations = arrayOf("filesystem:${rootProject.layout.projectDirectory.asFile}/src/main/resources/db/migration")
    cleanDisabled = false
}

tasks.named("compileKotlin") {
    dependsOn("flywayMigrate", "generateJooq")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    environment(dbEnv)
}

tasks.named<JavaExec>("run") {
    dependsOn("flywayMigrate")
    environment(dbEnv)
    systemProperties(dbEnv)
}

tasks.register("clearDb") {
    group = "database"
    description = "Drops all objects"
    dependsOn("flywayClean")
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveBaseName.set("server")
    archiveClassifier.set("all")
    archiveVersion.set("")
    mergeServiceFiles()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    dependsOn(project(":shared").tasks.named("openApiGenerate"))
    dependsOn(generateServerModels)
}

tasks.named("generateJooq") {
    dependsOn("compileJooqKotlin")
    dependsOn("flywayMigrate")
}

val generateServerModels by tasks.registering(JavaExec::class) {
    group = "codegen"
    description = "Run KotlinPoet generator for server-only models"
    classpath = project(":codegen").sourceSets["main"].runtimeClasspath
    mainClass.set("io.availe.ApplicationKt")
    workingDir = projectDir
}
