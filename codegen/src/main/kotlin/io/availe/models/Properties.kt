package io.availe.models

import com.squareup.kotlinpoet.TypeName

sealed class Property {
    abstract val name: String
    abstract val optional: Boolean
    abstract val replication: Replication

    data class Property(
        override val name: String,
        val underlyingType: TypeName,
        override val optional: Boolean,
        override val replication: Replication,
    ) : io.availe.models.Property()

    data class ForeignProperty(
        override val name: String,
        val property: Property,
        override val optional: Boolean,
        override val replication: Replication,
    ) : io.availe.models.Property()
}