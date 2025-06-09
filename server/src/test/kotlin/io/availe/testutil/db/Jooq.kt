package io.availe.testutil.db

import io.availe.testutil.TestConfig
import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.io.File
import java.sql.DriverManager

object Jooq {
    private val rootDir = File(System.getProperty("user.dir")).let {
        // if we are inside :server module adjust path one level up
        if (it.name == "server") it.parentFile else it
    }
    private val migrationPath = "filesystem:${rootDir.absolutePath}/src/main/resources/db/migration"

    init {
        val flyway = Flyway.configure()
            .dataSource(Postgres.jdbcUrl, Postgres.username, Postgres.password)
            .locations(migrationPath)
            .load()

        if (TestConfig.resetDb) flyway.clean()
        flyway.migrate()
    }

    private val conn = DriverManager.getConnection(Postgres.jdbcUrl, Postgres.username, Postgres.password)
    val dsl: DSLContext = DSL.using(conn, SQLDialect.POSTGRES)
}
