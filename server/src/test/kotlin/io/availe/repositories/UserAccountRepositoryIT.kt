@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package io.availe.repositories

import arrow.core.none
import arrow.core.some
import io.availe.models.*
import io.availe.testutil.TestFixtures
import io.availe.testutil.db.Jooq
import io.availe.testutil.tx.withRollback
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

class UserAccountRepositoryIT {

    private val repo = UserAccountRepository(Jooq.dsl)

    @Test
    fun `insert + fetchById round-trip`() = withRollback {
        val create = TestFixtures.newUserAccountCreate()
        val insertedEither = repo.insertUserAccount(create)
        assertTrue(insertedEither.isRight())
        val inserted = insertedEither.getOrNull()!!
        val fetchedOpt = repo.fetchById(inserted.id)
        assertTrue(fetchedOpt.isSome())
        val fetched = fetchedOpt.getOrNull()!!
        assertEquals(inserted.id, fetched.id)
        assertEquals(inserted.username, fetched.username)
        assertEquals(inserted.emailAddress, fetched.emailAddress)
        assertEquals(inserted.accountIsActive, fetched.accountIsActive)
        assertEquals(inserted.subscriptionTier, fetched.subscriptionTier)
        assertEquals(inserted.schemaVersion, fetched.schemaVersion)
    }

    @Test
    fun `insert duplicate email returns error`() = withRollback {
        val create = TestFixtures.newUserAccountCreate()
        assertTrue(repo.insertUserAccount(create).isRight())
        val second = repo.insertUserAccount(create)
        assertTrue(second.isLeft())
        assertEquals(UserAccountError.UserAlreadyExists, second.swap().getOrNull())
    }

    @Test
    fun `patchUserAccount updates fields`() = withRollback {
        val user = TestFixtures.insertUser(repo)
        val patch = UserAccountPatch(
            username = Username("renamed").some(),
            emailAddress = EmailAddress("new@example.com").some(),
            accountIsActive = AccountIsActive(false).some(),
            subscriptionTier = UserAccount.UserSubscriptionTier.BYOK.some(),
            schemaVersion = none()
        )
        assertTrue(repo.patchUserAccount(user.id, patch).isSome())
        val fetched = repo.fetchById(user.id).getOrNull()!!
        assertEquals("renamed", fetched.username.value)
        assertEquals("new@example.com", fetched.emailAddress.value)
        assertFalse(fetched.accountIsActive.value)
        assertEquals(UserAccount.UserSubscriptionTier.BYOK, fetched.subscriptionTier)
    }

    @Test
    fun `patchUserAccount unknown id returns none`() = withRollback {
        val noneOpt = repo.patchUserAccount(
            UserAccountId(Uuid.random()),
            UserAccountPatch(
                username = Username("x").some(),
                emailAddress = none(),
                accountIsActive = none(),
                subscriptionTier = none(),
                schemaVersion = none()
            )
        )
        assertTrue(noneOpt.isNone())
    }

    @Test
    fun `patchUserAccount empty patch returns none`() = withRollback {
        val user = TestFixtures.insertUser(repo)
        val noneOpt = repo.patchUserAccount(
            user.id,
            UserAccountPatch(
                username = none(),
                emailAddress = none(),
                accountIsActive = none(),
                subscriptionTier = none(),
                schemaVersion = none()
            )
        )
        assertTrue(noneOpt.isNone())
    }
}
