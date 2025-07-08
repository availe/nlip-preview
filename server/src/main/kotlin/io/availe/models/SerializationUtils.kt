package io.availe.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import java.util.UUID

/**--- UUID serializer ---*/
object UUIDSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: UUID) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): UUID = UUID.fromString(decoder.decodeString())
}

/**--- JSON config with UUID support ---*/
private val nlipSerializersModule = SerializersModule {
    contextual(UUIDSerializer)
}

internal val NLIP_JSON = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    serializersModule = nlipSerializersModule
}

