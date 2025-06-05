package io.availe.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jooq.SQLDialect
import org.jooq.impl.DSL

object DatabaseFactory {
    fun createDsl(environment: ApplicationEnvironment) = DSL.using(
        hikari(environment),
        SQLDialect.POSTGRES
    )

    private fun hikari(environment: ApplicationEnvironment): HikariDataSource {
        val config = environment.config

        val host = config.propertyOrNull("db.host")?.getString()
            ?: System.getenv("DB_HOST") ?: error("No DB_HOST")
        val port = config.propertyOrNull("db.port")?.getString()
            ?: System.getenv("DB_PORT") ?: "5432"
        val name = config.propertyOrNull("db.name")?.getString()
            ?: System.getenv("DB_NAME") ?: error("No DB_NAME")
        val schema = config.propertyOrNull("db.schema")?.getString()
            ?: System.getenv("DB_SCHEMA") ?: "public"
        val user = config.propertyOrNull("db.user")?.getString()
            ?: System.getenv("DB_USER") ?: error("No DB_USER")
        val pass = config.propertyOrNull("db.pass")?.getString()
            ?: System.getenv("DB_PASS") ?: ""

        val jdbcUrl = "jdbc:postgresql://$host:$port/$name?currentSchema=$schema"

        val maxPool = config.propertyOrNull("db.maximumPoolSize")?.getString()?.toIntOrNull() ?: 10
        val minIdle = config.propertyOrNull("db.minimumIdle")?.getString()?.toIntOrNull() ?: 2
        val idleTimeout = config.propertyOrNull("db.idleTimeout")?.getString()?.toLongOrNull() ?: 600_000L
        val maxLifetime = config.propertyOrNull("db.maxLifetime")?.getString()?.toLongOrNull() ?: 1_800_000L

        return HikariDataSource(HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            this.username = user
            this.password = pass
            this.maximumPoolSize = maxPool
            this.minimumIdle = minIdle
            this.idleTimeout = idleTimeout
            this.maxLifetime = maxLifetime
        })
    }
}
