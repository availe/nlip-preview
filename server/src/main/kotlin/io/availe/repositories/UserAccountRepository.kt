package io.availe.repositories

import arrow.core.*
import io.availe.models.UserAccount
import io.availe.models.UserId
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf

object UserAccountRepository {
    sealed class Error {
        object UserNotFound : Error()
        object AlreadyExists : Error()
    }

    private var store: PersistentMap<UserId, UserAccount> = persistentMapOf()

    fun findById(id: UserId): Option<UserAccount> =
        store[id].toOption()

    fun create(user: UserAccount): EitherNel<Error, Unit> =
        if (store.containsKey(user.id)) Error.AlreadyExists.leftNel()
        else {
            store = store.put(user.id, user)
            Unit.right()
        }

    fun delete(id: UserId): EitherNel<Error, Unit> =
        if (store.containsKey(id)) {
            store = store.remove(id)
            Unit.right()
        } else {
            Error.UserNotFound.leftNel()
        }

//    private fun updateField(
//        id: UserId,
//        update: (UserAccount) -> UserAccount
//    ): EitherNel<Error, Unit> =
//        findById(id).toEither { Error.UserNotFound }.map { update(it) }.map {
//            store = store.put(id, it)
//        }
//
//    //** ------------- CRUD operations ------------- */
//
//    fun updateEmail(id: UserId, newEmail: String): EitherNel<Error, Unit> =
//        updateField(id) { UserAccount.email.set(it, newEmail) }
//
//    fun updateUsername(id: UserId, newUsername: String): EitherNel<Error, Unit> =
//        updateField(id) { UserAccount.username.set(it, newUsername) }
//
//    fun updateLastLogin(id: UserId, newLoginTime: Instant): EitherNel<Error, Unit> =
//        updateField(id) { UserAccount.lastLoginAt.set(it, newLoginTime) }
//
//    fun setOnlineStatus(id: UserId, isOnline: Boolean): EitherNel<Error, Unit> =
//        updateField(id) { UserAccount.isOnline.set(it, isOnline) }
//
//    fun bumpVersion(id: UserId): EitherNel<Error, Unit> =
//        updateField(id) {
//            val currentVersion = it.version.value
//            UserAccount.version.set(it, UserAccountVersion(currentVersion + 1))
//        }
}