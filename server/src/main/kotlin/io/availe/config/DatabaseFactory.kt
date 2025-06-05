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

        val host = config.property("db.host").getString()
        val port = config.property("db.port").getString()
        val name = config.property("db.name").getString()
        val user = config.property("db.user").getString()
        val pass = config.property("db.pass").getString()
        val schema = config.propertyOrNull("db.schema")?.getString() ?: "public"

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
