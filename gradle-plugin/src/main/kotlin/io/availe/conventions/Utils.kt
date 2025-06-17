package io.availe.conventions

import io.availe.KReplicaExtension
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

fun registerKReplicaCodegenTask(
    project: Project,
    extension: KReplicaExtension
): TaskProvider<JavaExec> {
    val codegenConfiguration = project.configurations.maybeCreate("KReplicaCodegen")
    project.dependencies.add(codegenConfiguration.name, project.project(":codegen-runtime"))
    val runCodegen = project.tasks.register("runKReplicaCodegen", JavaExec::class.java) {
        group = "kreplica"
        description = "Runs the KReplica code generator"
        classpath = codegenConfiguration
        mainClass.set("io.availe.ApplicationKt")
        dependsOn(project.tasks.matching { it.name.startsWith("ksp") })
        doFirst {
            if (extension.modelJsonSources.isEmpty) {
                throw GradleException("KReplica: modelJsonSources is empty")
            }
            args(extension.modelJsonSources.files.map { it.absolutePath })
            if (extension.generatePatchable.get()) {
                args("--generate-patchable")
            }
        }
    }
    project.tasks.withType(KotlinCompilationTask::class.java).configureEach {
        dependsOn(runCodegen)
    }
    return runCodegen
}
