package io.availe.models

import kotlinx.serialization.Serializable

@Serializable
data class UserAccount(
    val userId: UserId,
    val username: Username,
    val emailAddress: EmailAddress,
    val accountIsActive: AccountIsActive,
    val userSubscriptionTier: UserSubscriptionTier,
) {
    enum class UserSubscriptionTier {
        STANDARD,
        BYOK,
        ENTERPRISE
    }
}