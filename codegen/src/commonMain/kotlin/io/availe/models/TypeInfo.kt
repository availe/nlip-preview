package io.availe.models

import kotlinx.serialization.Serializable

@Serializable
data class TypeInfo(
    val qualifiedName: String,
    val arguments: List<TypeInfo> = emptyList(),
    val isNullable: Boolean = false,
    val isEnum: Boolean = false
)