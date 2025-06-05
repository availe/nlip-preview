// file: server/src/main/kotlin/io/availe/config/DatabaseFactory.kt
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
        val cfg = environment.config

        // Try HOCON first; if it isn't defined, read from the OS env as a fallback:
        val host = cfg.propertyOrNull("db.host")?.getString()
            ?: System.getenv("DB_HOST")
            ?: error("DB_HOST must be set in ENV")

        val port = cfg.propertyOrNull("db.port")?.getString()
            ?: System.getenv("DB_PORT")
            ?: "5432"

        val name = cfg.propertyOrNull("db.name")?.getString()
            ?: System.getenv("DB_NAME")
            ?: error("DB_NAME must be set in ENV")

        val user = cfg.propertyOrNull("db.user")?.getString()
            ?: System.getenv("DB_USER")
            ?: error("DB_USER must be set in ENV")

        // Password can be empty, so default to empty string if nothing is provided
        val pass = cfg.propertyOrNull("db.pass")?.getString()
            ?: System.getenv("DB_PASS")
            ?: ""

        val schema = cfg.propertyOrNull("db.schema")?.getString()
            ?: System.getenv("DB_SCHEMA")
            ?: "public"

        val jdbcUrl = "jdbc:postgresql://$host:$port/$name?currentSchema=$schema"

        val maxPool = cfg.propertyOrNull("db.maximumPoolSize")?.getString()?.toIntOrNull() ?: 10
        val minIdle = cfg.propertyOrNull("db.minimumIdle")?.getString()?.toIntOrNull() ?: 2
        val idleTimeout = cfg.propertyOrNull("db.idleTimeout")?.getString()?.toLongOrNull() ?: 600_000L
        val maxLifetime = cfg.propertyOrNull("db.maxLifetime")?.getString()?.toLongOrNull() ?: 1_800_000L

        return HikariDataSource(
            HikariConfig().apply {
                this.jdbcUrl = jdbcUrl
                this.username = user
                this.password = pass
                this.maximumPoolSize = maxPool
                this.minimumIdle = minIdle
                this.idleTimeout = idleTimeout
                this.maxLifetime = maxLifetime
            }
        )
    }
}
