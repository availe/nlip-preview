package io.availe

import io.availe.generators.generateDataClasses
import io.availe.generators.generatePatchable
import io.availe.models.Model
import io.availe.utils.validateModelReplications
import kotlinx.serialization.json.Json
import java.io.File

fun main(args: Array<String>) {
    println("Availe Codegen Runtime starting...")
    val jsonPath = args.getOrNull(0) ?: error("Codegen Error: Missing models.json file path argument.")
    val jsonFile = File(jsonPath)
    require(jsonFile.exists()) { "Codegen Error: Specified models.json file does not exist: ${jsonFile.absolutePath}" }
    println("Loading model definitions from: ${jsonFile.path}")
    val models = Json.decodeFromString<List<Model>>(jsonFile.readText())
    println("Loaded ${models.size} model definitions.")
    validateModelReplications(models)
    println("Model definitions validated successfully.")
    generatePatchable()
    generateDataClasses(models)
    println("Code generation complete.")
}
