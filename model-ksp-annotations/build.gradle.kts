plugins { alias(libs.plugins.kotlinMultiplatform) }

group = "io.availe"
version = "1.0.0"

kotlin {
    jvm()                      // publishes JVM byte-code
    sourceSets {
        commonMain {
            kotlin.srcDir("src/main/kotlin")   // or move file as shown above
        }
        jvmMain { }            // inherits code from commonMain
    }
}
