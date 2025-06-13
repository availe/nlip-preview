package io.availe.helpers

import com.google.devtools.ksp.symbol.KSType

data class TypeInfo(
    val qualifiedName: String,
    val arguments: List<TypeInfo>,
    val isNullable: Boolean
) {
    val leafType: TypeInfo
        get() = if (arguments.isEmpty()) this else arguments.last().leafType

    companion object {
        fun from(ksType: KSType): TypeInfo {
            val qualifiedName = ksType.declaration.qualifiedName!!.asString()
            val arguments = ksType.arguments
                .mapNotNull { it.type?.resolve()?.let(::from) }
            val nullable = ksType.isMarkedNullable

            return TypeInfo(qualifiedName, arguments, nullable)
        }
    }
}