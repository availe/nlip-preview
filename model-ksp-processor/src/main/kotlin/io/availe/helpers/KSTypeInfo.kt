package io.availe.helpers

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier

data class KSTypeInfo(
    val qualifiedName: String,
    val arguments: List<KSTypeInfo>,
    val isNullable: Boolean,
    val isEnum: Boolean,
    val isValueClass: Boolean,
    val isDataClass: Boolean
) {
    val leafType: KSTypeInfo
        get() = if (arguments.isEmpty()) this else arguments.last().leafType

    companion object {
        private const val JVM_INLINE_ANNOTATION_FQN = "kotlin.jvm.JvmInline"

        fun from(ksType: KSType): KSTypeInfo {
            val decl = ksType.declaration as KSClassDeclaration
            val qualified = decl.qualifiedName!!.asString()
            val args = ksType.arguments.mapNotNull { it.type?.resolve()?.let(::from) }
            val nullable = ksType.isMarkedNullable
            val isEnum = decl.classKind == ClassKind.ENUM_CLASS
            val isData = decl.modifiers.contains(Modifier.DATA)

            val isValueByModifier = decl.modifiers.contains(Modifier.VALUE)
            val isValueByAnnotation = decl.annotations.any {
                it.annotationType.resolve().declaration.qualifiedName?.asString() == JVM_INLINE_ANNOTATION_FQN
            }
            val isValue = isValueByModifier || isValueByAnnotation

            println("KSTypeInfo.from qualifiedName=$qualified isEnum=$isEnum isValueClass=$isValue isDataClass=$isData")
            return KSTypeInfo(qualified, args, nullable, isEnum, isValue, isData)
        }
    }
}

fun KSTypeInfo.toModelTypeInfo(): io.availe.models.TypeInfo =
    io.availe.models.TypeInfo(
        qualifiedName = qualifiedName,
        arguments = arguments.map { it.toModelTypeInfo() },
        isNullable = isNullable,
        isEnum = isEnum,
        isValueClass = isValueClass,
        isDataClass = isDataClass
    )