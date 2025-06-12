package io.availe.models

import kotlinx.serialization.Serializable

@Serializable
sealed class Property {
    abstract val name: String
    abstract val replication: Replication
    abstract val annotations: List<AnnotationModel>?

    @Serializable
    data class Property(
        override val name: String,
        val underlyingType: String,
        override val replication: Replication,
        override val annotations: List<AnnotationModel>? = null,
    ) : io.availe.models.Property()

    @Serializable
    data class ForeignProperty(
        override val name: String,
        val foreignModelName: String,
        val property: Property,
        override val replication: Replication,
        override val annotations: List<AnnotationModel>? = null,
    ) : io.availe.models.Property()
}