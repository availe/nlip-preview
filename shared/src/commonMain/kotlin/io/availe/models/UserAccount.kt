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
    val id: Option<Long>
}
//
//@ModelGen
//interface InternalUserAccount {
//    val user: UserAccount
//}
//
//@ModelGen
//interface Cat {
//    val id: Long
//    @FieldGen(Replication.PATCH)
//    val user: UserAccount
//    @FieldGen(Replication.NONE)
//    val hello: String
//}