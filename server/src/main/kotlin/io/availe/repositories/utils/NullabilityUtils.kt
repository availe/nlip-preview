package io.availe.repositories.utils

import kotlin.reflect.KProperty1

fun <R, T> nn(value: T?, prop: KProperty1<R, T?>): T =
    checkNotNull(value) { "${prop.name} was null, but is non-nullable in the schema" }
