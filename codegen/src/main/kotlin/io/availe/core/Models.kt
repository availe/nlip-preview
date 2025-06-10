package io.availe.core

import com.squareup.kotlinpoet.TypeName

enum class Module { SHARED, SERVER }

enum class Replication { NONE, PATCH, CREATE, BOTH }

enum class Variant(val suffix: String) {
    BASE(""),
    CREATE("Create"),
    PATCH("Patch")
}

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
    val replication: Replication
) {
    init {
        val invalidPropertyItem = properties.firstOrNull { propertyItem ->
            !replication.allowedBy(propertyItem.replication)
        }
        val allowedReplications = when (replication) {
            Replication.NONE -> "NONE"
            Replication.PATCH -> "NONE, PATCH"
            Replication.CREATE -> "NONE, CREATE"
            Replication.BOTH -> "NONE, CREATE, PATCH, BOTH"
        }
        val errorMessage = """
            Invalid property replication in Model '$name':

              Property: '${invalidPropertyItem?.name}'
              Property Replication: ${invalidPropertyItem?.replication}
              Model Replication: $replication

            Allowed property replications for model with $replication: { $allowedReplications }

        """.trimIndent()
        require(invalidPropertyItem == null) { errorMessage }
    }
}

private fun Replication.allowedBy(childReplication: Replication): Boolean =
    when (this) {
        Replication.NONE -> childReplication == Replication.NONE
        Replication.PATCH -> childReplication == Replication.NONE || childReplication == Replication.PATCH
        Replication.CREATE -> childReplication == Replication.NONE || childReplication == Replication.CREATE
        Replication.BOTH -> true
    }
