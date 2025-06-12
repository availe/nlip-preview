@file:OptIn(ExperimentalUuidApi::class)

package io.availe.models

import io.availe.FieldGen
import io.availe.ModelGen
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ModelGen(annotations = [Serializable::class], optInMarkers = [ExperimentalUuidApi::class])
interface UserAccount {
    val id: Uuid
    val username: String
    val password: String
    val salt: String
    val md5: String
    val sha1: String
    val sha256: String
    val role: String
    val enabled: Boolean
    val accountNonExpired: Boolean
}

@ModelGen()
interface InternalUserAccount {
    val user: UserAccount

    @FieldGen(Replication.CREATE)
    val hash: String
}

@ModelGen()
interface OuterUserClass {
    val outerUser: InternalUserAccount
    val innerUser: UserAccount
}