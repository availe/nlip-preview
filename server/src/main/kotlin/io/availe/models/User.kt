import io.availe.Replicate
import io.availe.models.Variant

@Replicate.Model(variants = [Variant.BASE, Variant.CREATE, Variant.PATCH])
private interface AdminAccount {
    val user: UserAccountSchema.V1
}

private interface UserAccount

@Replicate.Model(variants = [Variant.BASE, Variant.CREATE, Variant.PATCH])
private interface V1 : UserAccount {
    val id: Int
}

