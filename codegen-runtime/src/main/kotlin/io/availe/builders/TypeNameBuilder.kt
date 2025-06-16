package io.availe.builders

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import io.availe.models.TypeInfo

fun TypeInfo.toTypeName(): TypeName {
    val fqName = this.qualifiedName
    val firstUpper = fqName.indexOfFirst { it.isUpperCase() }
    if (firstUpper == -1 || !fqName.contains('.')) {
        return ClassName("", fqName)
    }

    val pkg = fqName.substring(0, fqName.lastIndexOf('.', firstUpper))
    val simpleNames = fqName.substring(pkg.length + 1).split('.')
    val rawType = ClassName(pkg, simpleNames.first(), *simpleNames.drop(1).toTypedArray())

    if (this.arguments.isEmpty()) {
        return rawType.copy(nullable = this.isNullable)
    }

    val typeArguments = this.arguments.map { it.toTypeName() }
    return rawType.parameterizedBy(typeArguments).copy(nullable = this.isNullable)
}