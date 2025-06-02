package io.availe.config

import io.ktor.http.*

object NetworkConfig {
    val serverUrl = Url(SharedBuildConfig.BASE_URL)

    val SELF_HOSTS = setOf(
        "localhost",
        "127.0.0.1",
        "::1",
        serverUrl.host,
    )

    const val SELF_PORT = 8080
}
