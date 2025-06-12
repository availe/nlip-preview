package io.availe

import io.availe.generators.generateDataClasses
import io.availe.generators.generateInterfaces
import io.availe.generators.generateValueClasses
import io.availe.models.Model
import io.availe.utils.validateModelReplications
import kotlinx.serialization.json.Json
import java.io.File

object Paths {
    val sharedRoot = File("build/generated-src/kotlin-poet")
    val serverRoot = File("build/generated-src/kotlin-poet")
}

fun main(args: Array<String>) {
    println("Availe Codegen Runtime starting...")

    val jsonFilePath = args.getOrNull(0)
        ?: error("Codegen Error: Missing models.json file path argument.")

    val jsonFile = File(jsonFilePath)
    if (!jsonFile.exists()) {
        error("Codegen Error: Specified models.json file does not exist: ${jsonFile.absolutePath}")
    }

    println("Loading model definitions from: ${jsonFile.path}")
    val jsonData = jsonFile.readText()
    val modelDefinitions = Json.decodeFromString<List<Model>>(jsonData)

    println("Loaded ${modelDefinitions.size} model definitions.")

    validateModelReplications(modelDefinitions)
    println("Model definitions validated successfully.")

    generateInterfaces(modelDefinitions)
    generateValueClasses(modelDefinitions)
    generateDataClasses(modelDefinitions)
    println("Code generation complete.")
}