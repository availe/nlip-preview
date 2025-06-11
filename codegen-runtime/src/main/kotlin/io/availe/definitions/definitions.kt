package io.availe.definitions

import io.availe.models.Model
import io.availe.models.Module
import io.availe.models.Property
import io.availe.models.Replication

fun definitions(): List<Model> {
    val messageModelDefinition = Model(
        name = "Message",
        module = Module.SHARED,
        properties = listOf(
            Property.Property(
                name = "id",
                underlyingType = "kotlin.String",
                optional = false,
                replication = Replication.BOTH,
            )
        ),
        replication = Replication.BOTH
    )

    val internalMessageModelDefinition = Model(
        name = "InternalMessage",
        module = Module.SHARED,
        properties = listOf(
            Property.Property(
                name = "meta",
                // Use the fully-qualified class name as a String
                underlyingType = "kotlin.String",
                optional = true,
                replication = Replication.BOTH
            ),
            Property.ForeignProperty(
                name = "message",
                property = Property.Property(
                    name = "id",
                    // Use the fully-qualified class name as a String
                    underlyingType = "kotlin.String",
                    optional = false,
                    replication = Replication.BOTH
                ),
                optional = false,
                replication = Replication.BOTH
            )
        ),
        replication = Replication.BOTH
    )

    return listOf(messageModelDefinition, internalMessageModelDefinition)
}