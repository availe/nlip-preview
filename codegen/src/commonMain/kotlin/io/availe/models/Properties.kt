package io.availe.models

import kotlinx.serialization.Serializable

@Serializable
sealed class Property {
    abstract val name: String
    abstract val optional: Boolean
    abstract val replication: Replication

    @Serializable
    data class Property(
        override val name: String,
        val underlyingType: String,
        override val optional: Boolean,
        override val replication: Replication,
    ) : io.availe.models.Property()

    @Serializable
    data class ForeignProperty(
        override val name: String,
        val foreignModelName: String,
        val property: Property,
        override val optional: Boolean,
        override val replication: Replication,
    ) : io.availe.models.Property()
}