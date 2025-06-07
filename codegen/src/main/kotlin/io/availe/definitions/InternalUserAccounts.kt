package io.availe.definitions

import com.squareup.kotlinpoet.FileSpec
import io.availe.core.Module
import io.availe.core.codegen
import io.availe.core.generateDataClass
import io.availe.core.generateEnum
import java.io.File

fun generateInternalUserAccountModels() {
    val spec = codegen {
        enum(
            "UserRole",
            listOf("free_user", "paid_user", "admin"),
            nestedIn = "InternalUserAccount"
        )
        model("InternalUserAccount", module = Module.SERVER) {
            prop("userId", "UserAccountId")
            prop("passwordHash", "PasswordHash")
            prop("twoFactorEnabled", "TwoFactorEnabled")
            prop("twoFactorSecret", "TwoFactorSecret")
            prop("banTimestamp", "BanTimestamp")
            prop("banReason", "BanReason")
            prop("failedLoginAttemptCount", "FailedLoginAttemptCount")
            prop("lastFailedLoginTimestamp", "LastFailedLoginTimestamp")
            prop("accountLockedUntilTimestamp", "AccountLockedUntilTimestamp")
            prop("accountCreationTimestamp", "AccountCreationTimestamp")
            prop("lastPasswordChangeTimestamp", "LastPasswordChangeTimestamp")
            prop("lastLoginTimestamp", "LastLoginTimestamp")
            prop("lastSeenTimestamp", "LastSeenTimestamp")
            prop("lastModifiedByUserId", "UserAccountId")
            prop("lastModifiedTimestamp", "LastModifiedTimestamp")
            prop("userRole", "UserRole")
            prop("schemaVersion", "InternalUserAccountSchemaVersion")
        }
    }
    spec.models.forEach { model ->
        val dirPath = when (model.module) {
            Module.SHARED -> "../shared/build/generated-src/kotlin-poet/io/availe/models"
            Module.SERVER -> "../server/build/generated-src/kotlin-poet/io/availe/models"
        }
        val dir = File(dirPath).apply { mkdirs() }
        val nestedEnums = spec.enums.filter { it.nestedIn == model.name }
        val modelType = generateDataClass(model).toBuilder().apply {
            nestedEnums.forEach { addType(generateEnum(it)) }
        }.build()
        FileSpec.builder("io.availe.models", model.name)
            .addType(modelType)
            .build()
            .writeTo(dir)
    }
}
