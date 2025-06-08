package io.availe.infra

import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.sql.DriverManager

object Jooq {
    init {
        Flyway.configure()
            .dataSource(Postgres.jdbcUrl, Postgres.username, Postgres.password)
            .locations("classpath:db/migration")
            .load()
            .migrate()
    }

    private val conn = DriverManager.getConnection(Postgres.jdbcUrl, Postgres.username, Postgres.password)
    val dsl: DSLContext = DSL.using(conn, SQLDialect.POSTGRES)
}

inline fun <T> DSLContext.inTx(crossinline block: DSLContext.() -> T): T =
    transactionResult { cfg -> DSL.using(cfg).block() }
