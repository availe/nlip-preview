package io.availe.gradle

import io.availe.KReplicaExtension
import io.availe.ksp.commonMainKspDependencies
import io.availe.ksp.kspDependenciesForAllTargets
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun applyKmpConvention(project: Project, extension: KReplicaExtension) {
    if (!project.pluginManager.hasPlugin("com.google.devtools.ksp")) {
        project.pluginManager.apply("com.google.devtools.ksp")
    }
    val kmpExt = project.extensions.findByType(KotlinMultiplatformExtension::class.java) ?: return
    kmpExt.kspDependenciesForAllTargets {
        ksp(project.project(":model-ksp-processor"))
        ksp(project.project(":model-ksp-annotations"))
        ksp(project.project(":codegen"))
    }
    kmpExt.commonMainKspDependencies(project) { }
    registerKReplicaCodegenTask(project, extension)
    project.afterEvaluate {
        kmpExt.sourceSets.getByName("commonMain").kotlin.srcDir(
            project.layout.buildDirectory.dir("generated-src/kotlin-poet")
        )
        kmpExt.sourceSets.getByName("commonMain").kotlin.srcDir(
            project.layout.buildDirectory.dir("generated/kotlin-poet")
        )
    }
}
