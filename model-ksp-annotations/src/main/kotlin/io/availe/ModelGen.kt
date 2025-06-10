package io.availe

import io.availe.models.Module
import io.availe.models.Replication

/**
 * Marks a data class as a code-generation target.
 * @param module which output module (SHARED or SERVER)
 * @param replication which variants to generate (NONE, CREATE, PATCH, BOTH)
 */
@Target(AnnotationTarget.CLASS)
annotation class ModelGen(
    val module: Module = Module.SHARED,
    val replication: Replication = Replication.BOTH
)

/**
 * Override the class-level replication for this property.
 */
@Target(AnnotationTarget.PROPERTY)
annotation class FieldGen(val replication: Replication)
