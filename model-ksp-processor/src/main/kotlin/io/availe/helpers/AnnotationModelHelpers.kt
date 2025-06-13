package io.availe.helpers

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import io.availe.models.AnnotationArgument
import io.availe.models.AnnotationModel

fun Sequence<KSAnnotation>.toAnnotationModels(
    frameworkDecls: Set<KSClassDeclaration>
): List<AnnotationModel>? =
    mapNotNull { ann ->
        val decl = ann.annotationType.resolve().declaration as? KSClassDeclaration
            ?: return@mapNotNull null
        if (decl in frameworkDecls) return@mapNotNull null
        val args = ann.arguments.associate { arg ->
            val key = arg.name?.asString() ?: "value"
            val raw = arg.value
            key to when (raw) {
                is String -> AnnotationArgument.StringValue(raw)
                else -> AnnotationArgument.LiteralValue(raw.toString())
            }
        }
        AnnotationModel(decl.qualifiedName!!.asString(), args)
    }
        .toList()
        .takeIf { it.isNotEmpty() }
