@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package io.availe.repositories

import arrow.core.none
import arrow.core.some
import io.availe.models.*
import io.availe.testutil.TestFixtures
import io.availe.testutil.db.Jooq
import io.availe.testutil.tx.withRollback
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

class ConversationRepositoryIT {

    private val convoRepo = ConversationRepository(Jooq.dsl)
    private val userRepo = UserAccountRepository(Jooq.dsl)

    @Test
    fun `insert + fetchById round-trip`() = withRollback {
        val user = TestFixtures.insertUser(userRepo)
        val conv = TestFixtures.insertConversation(convoRepo, user.id)
        val fetchedOpt = convoRepo.fetchConversationById(conv.id)
        assertTrue(fetchedOpt.isSome())
        val fetched = fetchedOpt.getOrNull()!!
        assertEquals(conv.id, fetched.id)
        assertEquals(conv.title, fetched.title)
        assertEquals(conv.ownerId, fetched.ownerId)
        assertEquals(conv.status, fetched.status)
        assertEquals(conv.schemaVersion, fetched.schemaVersion)
        Unit
    }

    @Test
    fun `fetchAllUserConversationIds returns all ids`() = withRollback {
        val user = TestFixtures.insertUser(userRepo)
        val conv1 = TestFixtures.insertConversation(convoRepo, user.id)
        val conv2 = TestFixtures.insertConversation(convoRepo, user.id)
        val idsOpt = convoRepo.fetchAllUserConversationIds(user.id)
        assertTrue(idsOpt.isSome())
        val ids = idsOpt.getOrNull()!!.toSet()
        assertEquals(setOf(conv1.id, conv2.id), ids)
        Unit
    }

    @Test
    fun `fetchAllUserConversationIds none when no conversations`() = withRollback {
        val user = TestFixtures.insertUser(userRepo)
        val idsOpt = convoRepo.fetchAllUserConversationIds(user.id)
        assertTrue(idsOpt.isNone())
    }

    @Test
    fun `streamAllUserConversations emits all conversations`() = withRollback {
        val user = TestFixtures.insertUser(userRepo)
        val conv1 = TestFixtures.insertConversation(convoRepo, user.id)
        val conv2 = TestFixtures.insertConversation(convoRepo, user.id)
        val list = runBlocking { convoRepo.streamAllUserConversations(user.id).toList() }
        val ids = list.map(Conversation::id).toSet()
        assertEquals(setOf(conv1.id, conv2.id), ids)
        Unit
    }

    @Test
    fun `streamAllUserConversations empty when none`() = withRollback {
        val user = TestFixtures.insertUser(userRepo)
        val list = runBlocking { convoRepo.streamAllUserConversations(user.id).toList() }
        assertTrue(list.isEmpty())
    }

    @Test
    fun `patchConversation updates title and status`() = withRollback {
        val user = TestFixtures.insertUser(userRepo)
        val conv = TestFixtures.insertConversation(convoRepo, user.id)
        val newTitle = ConversationTitle("Renamed")
        val patch = ConversationPatch(
            title = newTitle.some(),
            status = Conversation.ConversationStatus.ARCHIVED.some(),
            schemaVersion = ConversationSchemaVersion(1).some()
        )
        val updatedOpt = convoRepo.patchConversation(conv.id, patch)
        assertTrue(updatedOpt.isSome())
        val fetched = convoRepo.fetchConversationById(conv.id).getOrNull()!!
        assertEquals(newTitle, fetched.title)
        assertEquals(Conversation.ConversationStatus.ARCHIVED, fetched.status)
        Unit
    }

    @Test
    fun `patchConversation unknown id returns none`() = withRollback {
        val patch = ConversationPatch(
            title = ConversationTitle("X").some(),
            status = none(),
            schemaVersion = none()
        )
        val noneOpt = convoRepo.patchConversation(ConversationId(Uuid.random()), patch)
        assertTrue(noneOpt.isNone())
    }

    @Test
    fun `patchConversation empty patch returns none`() = withRollback {
        val user = TestFixtures.insertUser(userRepo)
        val conv = TestFixtures.insertConversation(convoRepo, user.id)
        val noneOpt = convoRepo.patchConversation(
            conv.id,
            ConversationPatch(
                title = none(),
                status = none(),
                schemaVersion = none()
            )
        )
        assertTrue(noneOpt.isNone())
    }

    @Test
    fun `deleteConversationById removes row`() = withRollback {
        val user = TestFixtures.insertUser(userRepo)
        val conv = TestFixtures.insertConversation(convoRepo, user.id)
        val deletedOpt = convoRepo.deleteConversationById(conv.id)
        assertTrue(deletedOpt.isSome())
        assertTrue(convoRepo.fetchConversationById(conv.id).isNone())
        Unit
    }

    @Test
    fun `cascade delete conversations when user deleted`() = withRollback {
        val user = TestFixtures.insertUser(userRepo)
        val conv = TestFixtures.insertConversation(convoRepo, user.id)
        assertTrue(convoRepo.fetchConversationById(conv.id).isSome())
        assertTrue(userRepo.deleteUserAccount(user.id).isSome())
        assertTrue(convoRepo.fetchConversationById(conv.id).isNone())
        Unit
    }

    @Test
    fun `insertConversation rejects invalid status`() = withRollback {
        val user = TestFixtures.insertUser(userRepo)
        val create = TestFixtures.newConversationCreate(user.id).copy(
            status = Conversation.ConversationStatus.LOCAL
        )
        assertThrows(IllegalArgumentException::class.java) {
            convoRepo.insertConversation(create)
        }
        Unit
    }

    @Test
    fun `deleteConversationById missing id returns none`() = withRollback {
        val noneOpt = convoRepo.deleteConversationById(ConversationId(Uuid.random()))
        assertTrue(noneOpt.isNone())
        Unit
    }
}
