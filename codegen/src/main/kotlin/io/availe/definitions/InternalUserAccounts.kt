package io.availe.definitions

import io.availe.core.Module
import io.availe.core.codegen

fun generateInternalUserAccountModels() {
    val spec = codegen {
        enum("UserRole", listOf("free_user", "paid_user", "admin"), nestedIn = "InternalUserAccount")
        model("InternalUserAccount", module = Module.SERVER) {
            prop("userAccount", "UserAccount")
            prop("passwordHash", "PasswordHash", inPatch = true, inCreate = true)
            prop("twoFactorEnabled", "TwoFactorEnabled", inCreate = true, inPatch = true)
            prop("twoFactorSecret", "TwoFactorSecret", nullable = true, inCreate = false, inPatch = false)
            prop("banTimestamp", "BanTimestamp", nullable = true, inCreate = false, inPatch = false)
            prop("banReason", "BanReason", nullable = true, inCreate = false, inPatch = false)
            prop("failedLoginAttemptCount", "FailedLoginAttemptCount", inCreate = false, inPatch = false)
            prop("lastFailedLoginTimestamp", "LastFailedLoginTimestamp", nullable = true, inCreate = false)
            prop("accountLockedUntilTimestamp", "AccountLockedUntilTimestamp", nullable = true, inCreate = false)
            prop("accountCreationTimestamp", "AccountCreationTimestamp", inCreate = false, inPatch = false)
            prop("lastPasswordChangeTimestamp", "LastPasswordChangeTimestamp", nullable = true, inCreate = false)
            prop("lastLoginTimestamp", "LastLoginTimestamp", nullable = true, inCreate = false)
            prop("lastSeenTimestamp", "LastSeenTimestamp", nullable = true, inCreate = false)
            prop("lastModifiedByUserId", "UserAccountId", nullable = true, inCreate = false)
            prop("lastModifiedTimestamp", "LastModifiedTimestamp", nullable = true, inCreate = false)
            prop("userRole", "UserRole")
            prop("schemaVersion", "InternalUserAccountSchemaVersion", inCreate = true, inPatch = true)
        }
    }
    writeServerModels(spec, includeNestedEnums = true)
}
