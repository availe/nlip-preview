package io.availe.conventions

import io.availe.KReplicaExtension
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun applyKmpConvention(project: Project, extension: KReplicaExtension) {
    registerKReplicaCodegenTask(project, extension)
    project.afterEvaluate {
        val kmpExt = project.extensions.findByType(KotlinMultiplatformExtension::class.java)
        kmpExt?.sourceSets?.getByName("commonMain")?.kotlin?.srcDir(project.layout.buildDirectory.dir("generated/kotlin-poet"))
        kmpExt?.sourceSets?.getByName("commonMain")?.kotlin?.srcDir(project.layout.buildDirectory.dir("generated-src/kotlin-poet"))
    }
}
