package io.availe.models

import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
data class OutboundMessage(
    val targetUrl: Url,
    val internalMessage: InternalMessage,
    val version: OutboundMessageSchemaVersion
)
