package io.availe.helpers

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType

data class KSTypeInfo(
    val qualifiedName: String,
    val arguments: List<KSTypeInfo>,
    val isNullable: Boolean,
    val isEnum: Boolean
) {
    val leafType: KSTypeInfo
        get() = if (arguments.isEmpty()) this else arguments.last().leafType

    companion object Companion {
        fun from(ksType: KSType): KSTypeInfo {
            val declaration = ksType.declaration
            val qualifiedName = declaration.qualifiedName!!.asString()
            val arguments = ksType.arguments
                .mapNotNull { it.type?.resolve()?.let(::from) }
            val nullable = ksType.isMarkedNullable
            val isEnum = (declaration as? KSClassDeclaration)?.classKind == ClassKind.ENUM_CLASS

            return KSTypeInfo(qualifiedName, arguments, nullable, isEnum)
        }
    }
}

fun KSTypeInfo.toModelTypeInfo(): io.availe.models.TypeInfo =
    io.availe.models.TypeInfo(
        qualifiedName = qualifiedName,
        arguments = arguments.map { it.toModelTypeInfo() },
        isNullable = isNullable,
        isEnum = isEnum
    )