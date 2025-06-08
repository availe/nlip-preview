package io.availe.infra

import org.testcontainers.containers.PostgreSQLContainer

object Postgres : PostgreSQLContainer<Nothing>("postgres:17-alpine") {
    init {
        withDatabaseName("availe_test")
        withUsername("tester")
        withPassword("secret")
        withReuse(true)
        start()
    }
}
