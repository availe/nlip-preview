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

        val jdbcUrl = config.propertyOrNull("db.jdbcUrl")?.getString()
            ?.takeIf { it.isNotBlank() }
            ?: System.getenv("DB_JDBC_URL") ?: error("No DB_JDBC_URL")

        val user = config.propertyOrNull("db.user")?.getString()
            ?.takeIf { it.isNotBlank() }
            ?: System.getenv("DB_USER") ?: error("No DB_USER")

        val pass = config.propertyOrNull("db.pass")?.getString()
            ?.takeIf { it.isNotBlank() }
            ?: System.getenv("DB_PASS") ?: error("No DB_PASS")

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
