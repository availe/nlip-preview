package io.availe

import io.availe.models.Replication

@Target(AnnotationTarget.CLASS)
annotation class ModelGen(
    val replication: Replication = Replication.BOTH,
    val contextual: Boolean = true
)

@Target(AnnotationTarget.PROPERTY)
annotation class FieldGen(val replication: Replication)
