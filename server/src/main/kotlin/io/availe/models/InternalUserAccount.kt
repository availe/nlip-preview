package io.availe.models

import io.availe.ModelGen

@ModelGen(Replication.PATCH)
interface InternalUserAccount {
    val username: String
    val password: String
    val user: UserAccountId
}