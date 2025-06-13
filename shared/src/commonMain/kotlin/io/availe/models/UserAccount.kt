package io.availe.models

import io.availe.ModelGen
import kotlinx.serialization.Serializable

public interface UserAccount

@ModelGen(replication = Replication.PATCH, annotations = [Serializable::class])
public interface V1 : UserAccount {
    public val id: String
}
