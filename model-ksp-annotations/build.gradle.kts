plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

group = "io.availe"
version = "1.0.0"

kotlin {
    jvm()
    sourceSets {
        commonMain {
            kotlin.srcDir("src/main/kotlin")
            dependencies {
                implementation(project(":codegen"))
            }
        }
        jvmMain { }
    }
}