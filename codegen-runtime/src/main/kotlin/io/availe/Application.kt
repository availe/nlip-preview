package io.availe

import io.availe.generators.generateDataClasses
import io.availe.generators.generatePatchable
import io.availe.models.Model
import io.availe.utils.validateModelReplications
import kotlinx.serialization.json.Json
import java.io.File

fun main(args: Array<String>) {
    println("Availe Codegen Runtime starting...")

    val (flags, jsonPaths) = args.partition { it.startsWith("--") }

    if (jsonPaths.isEmpty()) {
        error("Codegen Error: Missing one or more models.json file path arguments.")
    }

    val shouldGeneratePatchable = flags.contains("--generate-patchable")

    println("Loading model definitions from: ${jsonPaths.joinToString()}")

    val primaryJsonPath = jsonPaths.first()
    val primaryModels = Json.decodeFromString<List<Model>>(File(primaryJsonPath).readText())

    val allModels = jsonPaths.flatMap { path ->
        val jsonFile = File(path)
        require(jsonFile.exists()) { "Codegen Error: Specified models.json file does not exist: ${jsonFile.absolutePath}" }
        Json.decodeFromString<List<Model>>(jsonFile.readText())
    }.distinctBy { it.packageName + "." + it.name }

    println("Loaded ${allModels.size} total model definitions. Will generate sources for ${primaryModels.size} primary models.")

    validateModelReplications(allModels)
    println("Model definitions validated successfully.")

    if (shouldGeneratePatchable) {
        println("Generating Patchable utility...")
        generatePatchable()
    }

    generateDataClasses(primaryModels, allModels)
    println("Code generation complete.")
}