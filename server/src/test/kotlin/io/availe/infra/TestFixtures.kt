package io.availe.infra

import io.availe.models.*
import io.availe.repositories.UserAccountRepository

object TestFixtures {
    private var userCounter = 1

    fun newUserAccountCreate(): UserAccountCreate {
        val idx = userCounter++
        return UserAccountCreate(
            username = Username("user$idx"),
            emailAddress = EmailAddress("user$idx@example.com"),
            accountIsActive = AccountIsActive(true),
            subscriptionTier = UserAccount.UserSubscriptionTier.STANDARD,
            schemaVersion = UserAccountSchemaVersion(1)
        )
    }

    fun insertUser(repo: UserAccountRepository): UserAccount =
        repo.insertUserAccount(newUserAccountCreate()).getOrNull()
            ?: error("Insertion failed in fixture")
}
