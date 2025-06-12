package io.availe.models

import arrow.core.Option
import io.availe.ModelGen
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi

@ModelGen(
    replication = Replication.PATCH,
    annotations = [Serializable::class],
    optInMarkers = [ExperimentalUuidApi::class]
)
interface UserAccount {
    val id: Option<String>
}
