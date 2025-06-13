package io.availe

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.*
import io.availe.helpers.*
import io.availe.models.AnnotationArgument
import io.availe.models.AnnotationModel
import io.availe.models.Replication

private val V_INT_REGEX = Regex("^V(\\d+)$")

internal fun fail(env: SymbolProcessorEnvironment, message: String): Nothing {
    env.logger.error(message)
    error(message)
}

internal fun KSAnnotation.isAnnotation(qualifiedName: String): Boolean {
    return this.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName
}

internal fun extractReplication(
    annotation: KSAnnotation,
    context: String,
    env: SymbolProcessorEnvironment
): Replication {
    val replicationArg = annotation.arguments.firstOrNull { it.name?.asString() == REPLICATION_ARG }
        ?: fail(env, "The @${annotation.shortName.asString()} on $context is missing the 'replication' argument.")

    val value = replicationArg.value ?: fail(env, "Value for 'replication' on $context is null.")

    val enumName = when (value) {
        is KSType -> value.declaration.simpleName.asString()
        is KSName -> value.asString().substringAfterLast('.')
        else -> value.toString().substringAfterLast('.')
    }

    return try {
        Replication.valueOf(enumName)
    } catch (e: IllegalArgumentException) {
        fail(
            env,
            "Invalid value for 'replication' on $context. Expected a valid Replication enum, but got '$enumName'."
        )
    }
}

internal fun extractAnnotations(
    declaration: KSClassDeclaration,
    modelAnn: KSAnnotation,
    frameworkDecls: Set<KSClassDeclaration>
): List<AnnotationModel>? {
    val directAnnotations = declaration.annotations.toAnnotationModels(frameworkDecls) ?: emptyList()

    val listedAnnotations = (modelAnn.arguments.find { it.name?.asString() == ANNOTATIONS_ARG }?.value as? List<*>)
        ?.mapNotNull { (it as? KSType)?.declaration?.qualifiedName?.asString() }
        ?.map { AnnotationModel(it) } ?: emptyList()

    return (directAnnotations + listedAnnotations).takeIf { it.isNotEmpty() }
}

internal fun extractOptInMarkers(modelAnn: KSAnnotation): List<String>? {
    return (modelAnn.arguments.find { it.name?.asString() == OPT_IN_MARKERS_ARG }?.value as? List<*>)
        ?.mapNotNull { (it as? KSType)?.declaration?.qualifiedName?.asString() }
        ?.takeIf { it.isNotEmpty() }
}

internal data class VersioningInfo(val baseModelName: String, val schemaVersion: Int)

internal fun determineVersioningInfo(
    declaration: KSClassDeclaration,
    env: SymbolProcessorEnvironment
): VersioningInfo? {
    val baseInterface = declaration.superTypes
        .mapNotNull { it.resolve().declaration as? KSClassDeclaration }
        .firstOrNull {
            it.classKind == ClassKind.INTERFACE && !it.annotations.any { ann ->
                ann.isAnnotation(
                    MODEL_ANNOTATION_NAME
                )
            }
        }
        ?: return null

    val explicitVersion = declaration.annotations
        .firstOrNull { it.isAnnotation(SCHEMA_VERSION_ANNOTATION_NAME) }
        ?.arguments
        ?.firstOrNull { it.name?.asString() == SCHEMA_VERSION_ARG }
        ?.value as? Int

    val inferredVersion = V_INT_REGEX.find(declaration.simpleName.asString())?.groupValues?.get(1)?.toIntOrNull()

    val version = explicitVersion ?: inferredVersion ?: fail(
        env,
        "Versioned model '${declaration.simpleName.asString()}' must either be named 'V<N>' (e.g., V1) " +
                "or be annotated with @SchemaVersion(number = N)."
    )

    return VersioningInfo(baseInterface.simpleName.asString(), version)
}

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
