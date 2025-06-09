package io.availe.testutil.tx

import io.availe.testutil.TestConfig
import io.availe.testutil.db.Jooq
import org.jooq.DSLContext
import org.jooq.impl.DSL

inline fun <T> withRollback(crossinline block: DSLContext.() -> T): T =
    if (TestConfig.rollbackEnabled) {
        Jooq.dsl.transactionResult { cfg ->
            val ctx = DSL.using(cfg)
            try {
                ctx.block()
            } finally {
                ctx.rollback()
            }
        }
    } else {
        Jooq.dsl.transactionResult { cfg -> DSL.using(cfg).block() }
    }
