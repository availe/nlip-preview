plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ksp)
    application
}

group = "io.availe"
version = "1.0.0"

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(projects.codegen)
}

application {
    mainClass.set("io.availe.ApplicationKt")
}
