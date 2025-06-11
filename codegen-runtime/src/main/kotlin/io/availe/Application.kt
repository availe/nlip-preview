package io.availe

import io.availe.definitions.definitions
import io.availe.generators.generateDataClasses
import io.availe.generators.generateInterfaces
import io.availe.generators.generateValueClasses
import io.availe.utils.validateModelReplications
import java.io.File

object Paths {
    val sharedRoot = File("../shared/build/generated-src/kotlin-poet")
    val serverRoot = File("../server/build/generated-src/kotlin-poet")
}

fun main() {
    val modelDefinitions = definitions()
    validateModelReplications(modelDefinitions)

    validateModelReplications(modelDefinitions)
    generateValueClasses(modelDefinitions)
    generateDataClasses(modelDefinitions)
}