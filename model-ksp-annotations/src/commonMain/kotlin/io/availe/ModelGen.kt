package io.availe

import io.availe.models.Replication
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
annotation class ModelGen(
    val replication: Replication = Replication.BOTH,
    val annotations: Array<KClass<out Annotation>> = [],
    val optInMarkers: Array<KClass<*>> = []
)

@Target(AnnotationTarget.PROPERTY)
annotation class FieldGen(
    val replication: Replication,
    val annotations: Array<KClass<out Annotation>> = []
)