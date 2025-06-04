package io.availe.models

import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
data class OutboundMessage(
    val targetUri: Url,
    val internalMessage: InternalMessage,
)
