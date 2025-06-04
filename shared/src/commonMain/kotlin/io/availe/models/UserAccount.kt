@file:OptIn(ExperimentalTime::class)

package io.availe.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class UserAccount(
    val id: UserId,
    val username: String,
    val email: String,
    @Contextual val createdAt: Instant,
    @Contextual val lastLoginAt: Instant,
    val isActive: Boolean = true
)
