package io.availe

import io.availe.generators.generateDataClasses
import io.availe.generators.generateValueClasses
import io.availe.utils.validateModelReplications
import io.availe.definitions.definitions
import java.io.File
import kotlin.uuid.ExperimentalUuidApi

object Paths {
    val sharedRoot = File("../shared/build/generated-src/kotlin-poet")
    val serverRoot = File("../server/build/generated-src/kotlin-poet")
}

@OptIn(ExperimentalUuidApi::class)
fun main() {
    val modelDefinitions = definitions()
    validateModelReplications(modelDefinitions)
    generateValueClasses(modelDefinitions)
    generateDataClasses(modelDefinitions)
}