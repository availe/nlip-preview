package io.availe.infra

import org.jooq.DSLContext
import org.jooq.impl.DSL

inline fun <T> withRollback(crossinline block: DSLContext.() -> T): T =
    Jooq.dsl.transactionResult { cfg ->
        val ctx = DSL.using(cfg)
        try {
            ctx.block()
        } finally {
            ctx.rollback()
        }
    }
