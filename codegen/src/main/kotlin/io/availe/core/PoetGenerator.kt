package io.availe.core

import com.squareup.kotlinpoet.typeNameOf
import kotlin.uuid.ExperimentalUuidApi

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
                replication = Replication.CREATE
            )
        ),
        replication = Replication.NONE
    )

    val internalMessageModelDefinition = Model(
        name = "InternalMessage",
        module = Module.SHARED,
        properties = listOf(
            Property.Property(
                name = "meta",
                underlyingType = typeNameOf<String>(),
                optional = false,
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

    modelDefinitions.forEach { modelParameter ->
        val baseProperties: List<Property> = fieldsForBase(modelParameter)
        val createProperties: List<Property> = fieldsForCreate(modelParameter)
        val patchProperties: List<Property> = fieldsForPatch(modelParameter)

        println("Generating for ${modelParameter.name}")
        println("Base:")
        baseProperties.forEach { propertyItem -> println(" - ${propertyItem.name}") }
        println("Create:")
        createProperties.forEach { propertyItem -> println(" - ${propertyItem.name}") }
        println("Patch:")
        patchProperties.forEach { propertyItem -> println(" - ${propertyItem.name}") }
        println("---")
    }
}
