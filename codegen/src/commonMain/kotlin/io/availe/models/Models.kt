package io.availe.models

import kotlinx.serialization.Serializable

@Serializable
data class Model(
    val name: String,
    val properties: List<Property>,
    val replication: Replication,
    val annotations: List<AnnotationModel>? = null,
    val optInMarkers: List<String>? = null,
) {
    init {
        val invalidProperty = properties.firstOrNull {
            !replication.allowedVariants(it.replication)
        }
        require(invalidProperty == null) {
            """
            Invalid property replication in model '$name':
              Property: '${invalidProperty?.name}'
              Property Replication: ${invalidProperty?.replication}
              Model Replication: $replication
            Allowed replications for model '$name': { ${replication.printAllowedVariants()} }
            """.trimIndent()
        }
    }
}

fun Replication.allowedVariants(child: Replication): Boolean =
    when (this) {
        Replication.NONE -> child == Replication.NONE
        Replication.PATCH -> child == Replication.NONE || child == Replication.PATCH
        Replication.CREATE -> child == Replication.NONE || child == Replication.CREATE
        Replication.BOTH -> true
    }

fun Replication.printAllowedVariants(): String = when (this) {
    Replication.NONE -> "NONE"
    Replication.PATCH -> "NONE, PATCH"
    Replication.CREATE -> "NONE, CREATE"
    Replication.BOTH -> "NONE, CREATE, PATCH, BOTH"
}