plugins {
    alias(libs.plugins.kotlinJvm)
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
}

group = "io.availe"
version = "1.0.0"

gradlePlugin {
    plugins {
        create("kreplica") {
            id = "io.availe.kreplica"
            implementationClass = "KReplicaPlugin"
        }
    }
}

dependencies {
    implementation(projects.codegen)
    implementation(projects.modelKspProcessor)
    implementation(projects.codegenRuntime)
    implementation(projects.modelKspAnnotations)
    compileOnly(libs.kotlin.gradle.plugin)
}

publishing {
    repositories {
        mavenLocal()
    }
}