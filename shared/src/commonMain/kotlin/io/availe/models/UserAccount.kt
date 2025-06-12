package io.availe.models

import io.availe.FieldGen
import io.availe.ModelGen

@ModelGen
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
}