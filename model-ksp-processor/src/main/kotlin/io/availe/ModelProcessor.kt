package io.availe

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import io.availe.helpers.*
import io.availe.models.AnnotationModel
import io.availe.models.Replication

class ModelProcessor(private val env: SymbolProcessorEnvironment) : SymbolProcessor {
    private var invoked: Boolean = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) return emptyList()

        val modelSymbols = resolver
            .getSymbolsWithAnnotation(MODEL_ANNOTATION_NAME)
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.INTERFACE }
            .filterNot { classDeclaration ->
                classDeclaration.annotations.any() { ann ->
                    ann.annotationType.resolve().declaration.qualifiedName?.asString() == HIDE_ANNOTATION_NAME
                }
            }
            .toList()

        if (modelSymbols.isEmpty()) return emptyList()

        val models = modelSymbols.map { symbol -> }
    }
}

private fun buildModel(
    declaration: KSClassDeclaration,
    resolver: Resolver
) {
    val modelAnnotation = declaration.annotations
        .first { it.annotationType.resolve().declaration.qualifiedName?.asString() == MODEL_ANNOTATION_NAME }

    val modelReplication = modelAnnotation.arguments
        .firstOrNull { it.name?.asString() == REPLICATION_ARG }
        ?.value
        ?.toString()
        ?.substringAfterLast('.')
        ?: Replication.BOTH

    val annotationsFromModelGenArg: List<AnnotationModel> =
        ((modelAnnotation.arguments
            .firstOrNull { it.name?.asString() == ANNOTATIONS_ARG }
            ?.value) as? List<*>)
            ?.mapNotNull { (it as? KSType)?.declaration?.qualifiedName?.asString() }
            ?.map { className -> AnnotationModel(className, emptyMap()) }
            ?: emptyList()

    val annotationsFromModelGenDiscovery = declaration.annotations
        .mapNotNull { ann ->
            val fullyQualifiedName = ann.annotationType.resolve()
                .declaration.qualifiedName?.asString()
                ?: return@mapNotNull null
            if (fullyQualifiedName in setOf(
                    MODEL_ANNOTATION_NAME,
                    FIELD_ANNOTATION_NAME,
                    SCHEMA_VERSION_ANNOTATION_NAME,
                )
            ) return@mapNotNull null
        }

}
