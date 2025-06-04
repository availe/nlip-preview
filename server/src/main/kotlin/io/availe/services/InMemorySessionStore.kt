package io.availe.services

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.right
import io.availe.models.BranchId
import io.availe.models.Conversation
import io.availe.models.InternalMessage
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

class InMemorySessionStore(
    private val maximumSessions: Int = 1000,
    private val maximumBranchesPerSession: Int = 1000,
    private val maximumMessagesPerBranch: Int = 1000
) {
    private val storeMutex = Mutex()
    private val data: ConcurrentHashMap<String, SessionState> = ConcurrentHashMap()

    internal data class SessionState(
        val lock: Mutex = Mutex(),
        var conversation: Conversation,
        var branches: PersistentMap<BranchId, PersistentMap<String, InternalMessage>>,
        val maximumBranches: Int,
        val maximumMessagesPerBranch: Int
    ) {
        fun send(
            branchIdentifier: BranchId,
            message: InternalMessage
        ): Either<ChatError, Unit> = either {
            val messages =
                branches[branchIdentifier] ?: raise(ChatError.BranchNotFound(conversation.id, branchIdentifier.value))
            ensure(messages.size < maximumMessagesPerBranch) {
                ChatError.BranchMessageLimitExceeded(conversation.id, branchIdentifier.value)
            }
            ensure(!messages.containsKey(message.id)) {
                ChatError.MessageAlreadyExists(conversation.id, message.id)
            }
            val newMessages = messages.put(message.id, message)
            branches = branches.put(branchIdentifier, newMessages)
            conversation = conversation.copy(lastActivityAt = message.timeStamp)
        }

        fun edit(
            branchIdentifier: BranchId,
            message: InternalMessage,
            forkBranch: Boolean
        ): Either<ChatError, BranchId> = either {
            val messages =
                branches[branchIdentifier] ?: raise(ChatError.BranchNotFound(conversation.id, branchIdentifier.value))
            ensure(messages.containsKey(message.id)) {
                ChatError.MessageNotFound(conversation.id, message.id)
            }
            if (forkBranch) {
                ensure(branches.size < maximumBranches) {
                    ChatError.BranchLimitExceeded(conversation.id)
                }
                var cloned = persistentMapOf<String, InternalMessage>()
                for ((key, value) in messages) {
                    if (key == message.id) {
                        cloned = cloned.put(key, message)
                        break
                    }
                    cloned = cloned.put(key, value)
                }
                val newBranchIdentifier = BranchId.random()
                branches = branches.put(newBranchIdentifier, cloned)
                conversation = conversation.copy(lastActivityAt = message.timeStamp)
                newBranchIdentifier
            } else {
                val newMessages = messages.put(message.id, message)
                branches = branches.put(branchIdentifier, newMessages)
                conversation = conversation.copy(lastActivityAt = message.timeStamp)
                branchIdentifier
            }
        }

        fun delete(
            branchIdentifier: BranchId,
            messageId: String,
            updateTimestamp: Long
        ): Either<ChatError, Unit> = either {
            val messages =
                branches[branchIdentifier] ?: raise(ChatError.BranchNotFound(conversation.id, branchIdentifier.value))
            ensure(messages.containsKey(messageId)) {
                ChatError.MessageNotFound(conversation.id, messageId)
            }
            val newMessages = messages.remove(messageId)
            branches = branches.put(branchIdentifier, newMessages)
            conversation = conversation.copy(lastActivityAt = updateTimestamp)
        }
    }

    suspend fun tryInsert(conversation: Conversation): Either<ChatError, Unit> = storeMutex.withLock {
        either {
            ensure(data.size < maximumSessions) {
                ChatError.SessionLimitExceeded(conversation.id)
            }
            ensure(
                data.putIfAbsent(
                    conversation.id,
                    SessionState(
                        conversation = conversation,
                        branches = persistentMapOf(BranchId.root to persistentMapOf()),
                        maximumBranches = maximumBranchesPerSession,
                        maximumMessagesPerBranch = maximumMessagesPerBranch
                    )
                ) == null
            ) {
                ChatError.SessionAlreadyExists(conversation.id)
            }
        }
    }

    suspend fun delete(sessionId: String): Either<ChatError, Unit> = storeMutex.withLock {
        if (data.remove(sessionId) != null) {
            Either.Right(Unit)
        } else {
            ChatError.SessionNotFound(sessionId).left()
        }
    }

    internal suspend fun <R> mutateSession(
        sessionId: String,
        block: suspend SessionState.() -> Either<ChatError, R>
    ): Either<ChatError, R> = storeMutex.withLock {
        either {
            val state = data[sessionId] ?: raise(ChatError.SessionNotFound(sessionId))
            state.lock.withLock {
                block(state).bind()
            }
        }
    }

    suspend fun snapshotBranches(sessionId: String): Either<ChatError, Map<BranchId, List<InternalMessage>>> =
        storeMutex.withLock {
            either {
                val state = data[sessionId] ?: raise(ChatError.SessionNotFound(sessionId))
                state.lock.withLock {
                    state.branches.mapValues { (_, map) -> map.values.toList() }
                }
            }
        }

    fun get(sessionId: String): Either<ChatError, Conversation> =
        data[sessionId]?.conversation?.right() ?: ChatError.SessionNotFound(sessionId).left()

    fun getAllSessionIds(): List<String> = data.keys.toList()

    /**
     * Updates the title of a session
     * @param sessionId ID of the session to update
     * @param newTitle New title for the session
     * @return Either with potential error or Unit on success
     */
    suspend fun updateSessionTitle(sessionId: String, newTitle: String): Either<ChatError, Unit> = storeMutex.withLock {
        either {
            val state = data[sessionId] ?: raise(ChatError.SessionNotFound(sessionId))
            state.lock.withLock {
                state.conversation = state.conversation.copy(
                    title = newTitle,
                    lastActivityAt = System.currentTimeMillis()
                )
            }
        }
    }
}
