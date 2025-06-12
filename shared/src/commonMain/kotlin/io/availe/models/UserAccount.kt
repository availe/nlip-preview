package io.availe.models

import io.availe.FieldGen
import io.availe.ModelGen

@ModelGen
interface UserAccount {
    val id: Long
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
}