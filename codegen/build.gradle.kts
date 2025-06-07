plugins {
    alias(libs.plugins.kotlinJvm)
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

    implementation(libs.kotlinpoet)
}