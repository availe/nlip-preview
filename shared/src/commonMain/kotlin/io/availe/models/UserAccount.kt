package io.availe.models

import io.availe.FieldGen
import io.availe.ModelGen
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi

@ModelGen(annotations = [Serializable::class], optInMarkers = [ExperimentalUuidApi::class])
@Deprecated("Use InternalUserAccount instead")
interface UserAccount {
    @FieldGen(Replication.NONE)
    val id: Long
    @FieldGen(Replication.NONE)
    val name: String
}

@ModelGen
interface InternalUserAccount {
    val user: UserAccount
}

@ModelGen
interface Cat {
    val id: Long
    @FieldGen(Replication.PATCH)
    val user: UserAccount
    @FieldGen(Replication.NONE)
    val hello: String
}