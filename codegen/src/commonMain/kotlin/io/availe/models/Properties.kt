package io.availe.models

import kotlinx.serialization.Serializable

@Serializable
sealed class Property {
    abstract val name: String
    abstract val typeInfo: TypeInfo
    abstract val replication: Replication
    abstract val annotations: List<AnnotationModel>?

    @Serializable
    data class Property(
        override val name: String,
        override val typeInfo: TypeInfo,
        override val replication: Replication,
        override val annotations: List<AnnotationModel>? = null,
    ) : io.availe.models.Property()

    @Serializable
    data class ForeignProperty(
        override val name: String,
        override val typeInfo: TypeInfo,
        val foreignModelName: String,
        override val replication: Replication,
        override val annotations: List<AnnotationModel>? = null,
    ) : io.availe.models.Property()
}