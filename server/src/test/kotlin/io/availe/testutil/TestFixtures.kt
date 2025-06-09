package io.availe.testutil

import io.availe.models.*
import io.availe.repositories.UserAccountRepository

object TestFixtures {
    private var userCounter = 1

    fun newUserAccountCreate(): UserAccountCreate {
        val n = userCounter++
        return UserAccountCreate(
            username = Username("user$n"),
            emailAddress = EmailAddress("user$n@example.com"),
            accountIsActive = AccountIsActive(true),
            subscriptionTier = UserAccount.UserSubscriptionTier.STANDARD,
            schemaVersion = UserAccountSchemaVersion(1)
        )
    }

    fun insertUser(repo: UserAccountRepository): UserAccount =
        repo.insertUserAccount(newUserAccountCreate()).getOrNull()
            ?: error("failed to insert user")
}
