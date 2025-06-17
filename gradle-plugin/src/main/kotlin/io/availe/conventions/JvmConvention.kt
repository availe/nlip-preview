package io.availe.conventions

import io.availe.KReplicaExtension
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

fun applyJvmConvention(project: Project, extension: KReplicaExtension) {
    registerKReplicaCodegenTask(project, extension)
    project.afterEvaluate {
        val jvmExt = project.extensions.findByType(KotlinJvmProjectExtension::class.java)
        jvmExt?.sourceSets
            ?.getByName("main")
            ?.kotlin
            ?.srcDir(project.layout.buildDirectory.dir("generated-src/kotlin-poet"))
    }
}