package io.availe.models

import io.availe.FieldGen
import io.availe.ModelGen
import io.availe.SchemaVersion
import kotlinx.serialization.Serializable

public interface UserAccount

@ModelGen(replication = Replication.BOTH, annotations = [Serializable::class])
public interface V1 : UserAccount {
    @FieldGen(replication = Replication.BOTH)
    public val id: String
}

@ModelGen(replication = Replication.BOTH, annotations = [Serializable::class])
public interface V2 : UserAccount {
    @FieldGen(replication = Replication.BOTH)
    public val id: String

    @FieldGen(replication = Replication.CREATE)
    public val email: String
}

@ModelGen(replication = Replication.BOTH, annotations = [Serializable::class])
@SchemaVersion(number = 0)
public interface Legacy : UserAccount {
    public val legacyId: Int
}