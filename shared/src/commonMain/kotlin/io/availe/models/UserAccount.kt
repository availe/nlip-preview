@file:OptIn(ExperimentalUuidApi::class)

package io.availe.models

import io.availe.ModelGen
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

public interface UserAccount

@ModelGen(
    replication = Replication.PATCH,
    annotations = [Serializable::class],
    optInMarkers = [ExperimentalUuidApi::class]
)
public interface V1 : UserAccount {
    @OptIn(ExperimentalUuidApi::class)
    public val id: Uuid

    @OptIn(ExperimentalTime::class)
    @Contextual
    public val time: kotlin.time.Instant
}

@ModelGen(
    Replication.NONE,
    optInMarkers = [ExperimentalUuidApi::class]
)
interface Cat {
    val id: Uuid
}