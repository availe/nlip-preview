package io.availe.infra

import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.io.File
import java.sql.DriverManager

object Jooq {
    private val rootDir: File = File(System.getProperty("user.dir")).let { dir ->
        if (dir.name == "server") dir.parentFile else dir
    }
    private val migrationPath = "filesystem:${rootDir.absolutePath}/src/main/resources/db/migration"

    init {
        Flyway.configure()
            .dataSource(Postgres.jdbcUrl, Postgres.username, Postgres.password)
            .locations(migrationPath)
            .load()
            .migrate()
    }

    private val conn = DriverManager.getConnection(Postgres.jdbcUrl, Postgres.username, Postgres.password)
    val dsl: DSLContext = DSL.using(conn, SQLDialect.POSTGRES)
}
