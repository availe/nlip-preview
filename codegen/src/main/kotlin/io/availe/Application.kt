package io.availe

import com.squareup.kotlinpoet.typeNameOf
import io.availe.generators.generateDataClasses
import io.availe.generators.generateValueClasses
import io.availe.models.Model
import io.availe.models.Module
import io.availe.models.Property
import io.availe.models.Replication
import io.availe.utils.validateModelReplications
import java.io.File
import kotlin.uuid.ExperimentalUuidApi

object Paths {
    val sharedRoot = File("../shared/build/generated-src/kotlin-poet")
    val serverRoot = File("../server/build/generated-src/kotlin-poet")
}

@OptIn(ExperimentalUuidApi::class)
fun main() {
    val messageModelDefinition = Model(
        name = "Message",
        module = Module.SHARED,
        properties = listOf(
            Property.Property(
                name = "id",
                underlyingType = typeNameOf<String>(),
                optional = false,
                replication = Replication.PATCH,
            )
        ),
        replication = Replication.PATCH
    )

    val internalMessageModelDefinition = Model(
        name = "InternalMessage",
        module = Module.SHARED,
        properties = listOf(
            Property.Property(
                name = "meta",
                underlyingType = typeNameOf<String>(),
                optional = true,
                replication = Replication.BOTH
            ),
            Property.ForeignProperty(
                name = "message",
                property = Property.Property(
                    name = "id",
                    underlyingType = typeNameOf<String>(),
                    optional = false,
                    replication = Replication.BOTH
                ),
                optional = false,
                replication = Replication.BOTH
            )
        ),
        replication = Replication.BOTH
    )

    val modelDefinitions = listOf(messageModelDefinition, internalMessageModelDefinition)

    validateModelReplications(modelDefinitions)
    generateValueClasses(modelDefinitions)
    generateDataClasses(modelDefinitions)
}