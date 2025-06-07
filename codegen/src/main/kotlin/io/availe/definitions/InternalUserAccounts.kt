package io.availe.definitions

import com.squareup.kotlinpoet.FileSpec
import io.availe.core.codegen
import io.availe.core.generateDataClass
import java.io.File

fun generateInternalUserAccountModels() {
    val out = File("../shared/build/generated-src/kotlin-poet/io/availe/models").apply { mkdirs() }
    val spec = codegen {
        model("InternalUserAccount") {
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
        FileSpec.builder("io.availe.models", model.name)
            .addType(generateDataClass(model))
            .build()
            .writeTo(out)
    }
}
