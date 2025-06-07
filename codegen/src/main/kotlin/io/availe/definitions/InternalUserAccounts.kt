package io.availe.definitions

import com.squareup.kotlinpoet.FileSpec
import io.availe.core.Module
import io.availe.core.codegen
import io.availe.core.generateDataClass
import io.availe.core.generateEnum
import java.io.File

fun generateInternalUserAccountModels() {
    val spec = codegen {
        enum("UserRole", listOf("free_user", "paid_user", "admin"), nestedIn = "InternalUserAccount")
        model("InternalUserAccount", module = Module.SERVER) {
            prop("userAccount", "UserAccount")
            prop("passwordHash", "PasswordHash")
            prop("twoFactorEnabled", "TwoFactorEnabled")
            prop("twoFactorSecret", "TwoFactorSecret", nullable = true)
            prop("banTimestamp", "BanTimestamp", nullable = true)
            prop("banReason", "BanReason", nullable = true)
            prop("failedLoginAttemptCount", "FailedLoginAttemptCount")
            prop("lastFailedLoginTimestamp", "LastFailedLoginTimestamp", nullable = true)
            prop("accountLockedUntilTimestamp", "AccountLockedUntilTimestamp", nullable = true)
            prop("accountCreationTimestamp", "AccountCreationTimestamp")
            prop("lastPasswordChangeTimestamp", "LastPasswordChangeTimestamp", nullable = true)
            prop("lastLoginTimestamp", "LastLoginTimestamp", nullable = true)
            prop("lastSeenTimestamp", "LastSeenTimestamp", nullable = true)
            prop("registrationIpAddress", "RegistrationIpAddress")
            prop("lastLoginIpAddress", "LastLoginIpAddress", nullable = true)
            prop("previousLoginIpAddresses", "PreviousLoginIpAddresses")
            prop("knownDeviceTokens", "KnownDeviceTokens")
            prop("lastModifiedByUserId", "UserAccountId", nullable = true)
            prop("lastModifiedTimestamp", "LastModifiedTimestamp", nullable = true)
            prop("userRole", "UserRole")
            prop("schemaVersion", "InternalUserAccountSchemaVersion")
        }
    }
    spec.models.forEach { model ->
        val outDir = File("../server/build/generated-src/kotlin-poet/io/availe/models").apply { mkdirs() }
        val nestedEnums = spec.enums.filter { it.nestedIn == model.name }
        val modelType = generateDataClass(model).toBuilder().apply {
            nestedEnums.forEach { addType(generateEnum(it)) }
        }.build()
        FileSpec.builder("io.availe.models", model.name)
            .addType(modelType)
            .build()
            .writeTo(outDir)
    }
}
