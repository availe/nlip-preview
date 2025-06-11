package io.availe.models

import io.availe.ModelGen

@ModelGen
data class InternalUserAccount(val username: String, val password: String)