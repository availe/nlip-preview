package io.availe.gradle

import io.availe.KReplicaExtension
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

fun applyJvmConvention(project: Project, extension: KReplicaExtension) {
    if (!project.pluginManager.hasPlugin("com.google.devtools.ksp")) {
        project.pluginManager.apply("com.google.devtools.ksp")
    }
    project.dependencies.add("ksp", project.project(":model-ksp-processor"))
    project.dependencies.add("ksp", project.project(":model-ksp-annotations"))
    project.dependencies.add("ksp", project.project(":codegen"))
    registerKReplicaCodegenTask(project, extension)
    project.afterEvaluate {
        val jvmExt = project.extensions.findByType(KotlinJvmProjectExtension::class.java)
        jvmExt?.sourceSets?.getByName("main")?.kotlin?.srcDir(
            project.layout.buildDirectory.dir("generated-src/kotlin-poet")
        )
    }
}
