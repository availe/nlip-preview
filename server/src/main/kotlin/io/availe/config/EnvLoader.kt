package io.availe.config

import io.github.cdimascio.dotenv.Dotenv
import java.io.File

object EnvLoader {
    private val MARKERS = listOf(".git", "settings.gradle.kts")

    fun load() {
        val projectRoot = findProjectRoot()
        Dotenv.configure()
            .directory(projectRoot)
            .ignoreIfMalformed()
            .ignoreIfMissing()
            .load()
    }

    private fun findProjectRoot(): String {
        var dir = File(System.getProperty("user.dir"))
        while (dir.parentFile != null) {
            if (MARKERS.any { File(dir, it).exists() }) {
                break
            }
            dir = dir.parentFile
        }
        return dir.absolutePath
    }
}
