fun loadSecrets(fileName: String = ".env"): Map<String, String> =
    rootProject.file(fileName).let { f ->
        if (!f.exists()) error("$fileName missing")
        f.readLines()
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.startsWith("#") && "=" in it }
            .associate { line ->
                val (k, v) = line.split("=", limit = 2)
                k.trim() to v.trim()
            }
    }

val secrets = loadSecrets()
extra["secrets"] = secrets
subprojects { extra["secrets"] = secrets }

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.versions)
    alias(libs.plugins.versionCatalogUpdate)
}

subprojects {
    apply(plugin = "com.github.ben-manes.versions")
}

tasks.register("checkDependencyUpdates") {
    group = "versioning"
    description = "Checks for dependency updates (does not apply them)."

    dependsOn(":dependencyUpdates")
}