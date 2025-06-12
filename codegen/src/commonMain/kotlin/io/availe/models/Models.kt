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
        require(properties.isNotEmpty()) {
            "Model validation failed for '$name': Model interfaces cannot be empty and must contain at least one property."
        }

        val invalidProperties = properties.filter {
            !replication.allowedVariants(it.replication)
        }
        require(invalidProperties.isEmpty()) {
            val count = invalidProperties.size
            val pluralS = if (count == 1) "" else "s"
            val noun = if (count == 1) "property" else "properties"
            val verb = if (count == 1) "is" else "are"

            val propertiesReport = invalidProperties.joinToString("\n") {
                "  - Property: '${it.name}' (has replication '${it.replication}')"
            }
            """
            Invalid property replication$pluralS found in model '$name':
            The model's replication level is '$replication', which only allows properties with replication levels of { ${replication.printAllowedVariants()} }.

            The following $noun $verb invalid:
            $propertiesReport
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