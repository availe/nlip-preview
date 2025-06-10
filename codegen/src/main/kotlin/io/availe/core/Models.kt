package io.availe.core

import com.squareup.kotlinpoet.TypeName

enum class Module { SHARED, SERVER }

enum class Replication { NONE, PATCH, CREATE, BOTH }

sealed class Property {
    abstract val name: String
    abstract val optional: Boolean
    abstract val replication: Replication

    data class Property(
        override val name: String,
        val underlyingType: TypeName,
        override val optional: Boolean,
        override val replication: Replication
    ) : io.availe.core.Property()

    data class ForeignProperty(
        override val name: String,
        val property: Property,
        override val optional: Boolean,
        override val replication: Replication
    ) : io.availe.core.Property()
}

data class Model(
    val name: String,
    val module: Module,
    val properties: List<Property>,
    val replication: Replication,
)