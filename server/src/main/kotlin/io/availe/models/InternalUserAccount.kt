package io.availe.models

import io.availe.ModelGen

@ModelGen
interface InternalUserAccount {
    val username: String
    val password: String
}