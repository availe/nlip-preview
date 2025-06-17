package io.availe

import io.availe.gradle.applyJvmConvention
import io.availe.gradle.applyKmpConvention
import org.gradle.api.Plugin
import org.gradle.api.Project

class KReplicaPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create("kreplica", KReplicaExtension::class.java, target.objects, target)
        val isKmp = target.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")
        val isJvm = target.plugins.hasPlugin("org.jetbrains.kotlin.jvm") && !isKmp
        when {
            isKmp -> applyKmpConvention(target, extension)
            isJvm -> applyJvmConvention(target, extension)
            else -> target.logger.warn("KReplica: supported Kotlin plugin not found; no convention applied")
        }
    }
}
