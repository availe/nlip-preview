package io.availe.builders

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import io.availe.models.TypeInfo

fun TypeInfo.toTypeName(baseName: String? = null, versionName: String? = null): TypeName {
    val rawType = if (baseName != null && versionName != null) {
        val simpleName = this.qualifiedName.substringAfterLast('.')
        ClassName(this.qualifiedName.substringBeforeLast('.'), baseName, versionName, simpleName)
    } else {
        ClassName(
            this.qualifiedName.substringBeforeLast('.'),
            this.qualifiedName.substringAfterLast('.')
        )
    }

    if (this.arguments.isEmpty()) {
        return rawType.copy(nullable = this.isNullable)
    }

    val typeArgs = this.arguments.map { it.toTypeName() }
    return rawType.parameterizedBy(typeArgs).copy(nullable = this.isNullable)
}