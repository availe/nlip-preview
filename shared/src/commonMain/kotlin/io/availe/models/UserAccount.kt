package io.availe.models

import io.availe.ModelGen
import kotlinx.serialization.Serializable
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
}

@ModelGen(
    Replication.NONE,
)
interface Cat {
    val id: String
}