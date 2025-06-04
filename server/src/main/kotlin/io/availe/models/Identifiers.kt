@file:OptIn(ExperimentalUuidApi::class)

package io.availe.models

import de.mkammerer.argon2.Argon2Factory
import java.net.InetAddress
import java.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@JvmInline
value class PasswordHash private constructor(val value: String) {
    companion object {
        fun hashRaw(rawPassword: String): PasswordHash {
            require(rawPassword.isNotBlank())
            require(rawPassword.length >= 8)
            val argon2 = Argon2Factory.create()
            val iterations = 3
            val memory = 65536
            val parallelism = 1
            val hash = argon2.hash(iterations, memory, parallelism, rawPassword.toCharArray())
            argon2.wipeArray(rawPassword.toCharArray())
            return PasswordHash(hash)
        }

        fun fromStored(stored: String): PasswordHash {
            return PasswordHash(stored)
        }
    }

    fun matches(rawPassword: String): Boolean {
        val argon2 = Argon2Factory.create()
        return argon2.verify(value, rawPassword.toCharArray())
    }
}

@JvmInline
value class TwoFactorEnabled(val value: Boolean)

@JvmInline
value class TwoFactorSecret private constructor(val value: String) {
    companion object {
        private val BASE32_REGEX = Regex("^[A-Z2-7]{16,64}$")
        fun fromBase32(raw: String): TwoFactorSecret {
            val upper = raw.trim().uppercase()
            require(upper.matches(BASE32_REGEX))
            return TwoFactorSecret(upper)
        }
    }
}

@JvmInline
value class BanTimestamp(val value: Instant)

@JvmInline
value class LastFailedLoginTimestamp(val value: Instant)

@JvmInline
value class AccountLockedUntilTimestamp(val value: Instant)

@JvmInline
value class AccountCreationTimestamp(val value: Instant)

@JvmInline
value class LastPasswordChangeTimestamp(val value: Instant)

@JvmInline
value class LastLoginTimestamp(val value: Instant)

@JvmInline
value class LastSeenTimestamp(val value: Instant)

@JvmInline
value class LastModifiedTimestamp(val value: Instant)

@JvmInline
value class BanReason(val value: String) {
    init {
        require(value.isNotBlank())
        require(value.length <= 256)
        require(!value.contains('\n'))
    }
}

@JvmInline
value class FailedLoginAttemptCount private constructor(val value: Int) {
    companion object {
        fun from(raw: Int): FailedLoginAttemptCount {
            require(raw in 0..10)
            return FailedLoginAttemptCount(raw)
        }
    }
}

@JvmInline
value class RegistrationIpAddress(val value: InetAddress) {
    companion object {
        fun fromString(hostOrIp: String): RegistrationIpAddress =
            RegistrationIpAddress(InetAddress.getByName(hostOrIp))
    }
}

@JvmInline
value class LastLoginIpAddress(val value: InetAddress) {
    companion object {
        fun fromString(hostOrIp: String): LastLoginIpAddress =
            LastLoginIpAddress(InetAddress.getByName(hostOrIp))
    }
}

@JvmInline
value class PreviousLoginIpAddresses(val value: List<InetAddress>) {
    companion object {
        fun fromStrings(rawList: List<String>): PreviousLoginIpAddresses =
            PreviousLoginIpAddresses(rawList.map { InetAddress.getByName(it) })
    }
}

@JvmInline
value class DeviceToken private constructor(val uuid: Uuid) {
    companion object {
        fun fromUuid(uuid: Uuid): DeviceToken = DeviceToken(uuid)
        fun fromString(raw: String): DeviceToken = DeviceToken(Uuid.parse(raw))
        fun random(): DeviceToken = DeviceToken(Uuid.random())
    }

    override fun toString(): String = uuid.toString()
}

@JvmInline
value class KnownDeviceTokens(val value: List<Uuid>)

@JvmInline
value class UserAccountVersion(val value: Int) {
    init {
        require(value >= 1)
    }
}
