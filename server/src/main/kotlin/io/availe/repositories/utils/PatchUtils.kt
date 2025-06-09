package io.availe.repositories.utils

import arrow.core.Option
import org.jooq.Field

internal inline fun <A> MutableMap<Field<*>, Any>.putIfSome(
    opt: Option<A>,
    field: Field<*>,
    transform: (A) -> Any = { it as Any }
) {
    opt.getOrNull()?.let { this[field] = transform(it) }
}
