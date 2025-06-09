package io.availe.repositories

import io.availe.testutil.TestFixtures
import io.availe.testutil.db.Jooq
import io.availe.testutil.tx.withRollback
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

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
}
