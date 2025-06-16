package io.availe.models

import io.availe.ModelGen

@ModelGen(Replication.BOTH)
interface InternalUserAccount {
    val username: String
    val conversationId: ConversationId
}