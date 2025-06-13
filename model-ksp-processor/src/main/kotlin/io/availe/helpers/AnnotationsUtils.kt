package io.availe

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.*
import io.availe.helpers.*
import io.availe.models.AnnotationArgument
import io.availe.models.AnnotationModel
import io.availe.models.Replication

private val V_INT_REGEX = Regex("^V(\\d+)$")

internal fun fail(environment: SymbolProcessorEnvironment, message: String): Nothing {
    environment.logger.error(message)
    error(message)
}

internal fun KSAnnotation.isAnnotation(qualifiedName: String): Boolean {
    return this.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName
}

internal fun extractReplication(
    annotation: KSAnnotation,
    context: String,
    environment: SymbolProcessorEnvironment
): Replication {
    val replicationArgument = annotation.arguments.firstOrNull { it.name?.asString() == REPLICATION_ARG }
        ?: fail(
            environment,
            "The @${annotation.shortName.asString()} on $context is missing the 'replication' argument."
        )

    val value = replicationArgument.value ?: fail(environment, "Value for 'replication' on $context is null.")

    val enumName = when (value) {
        is KSType -> value.declaration.simpleName.asString()
        is KSName -> value.asString().substringAfterLast('.')
        else -> value.toString().substringAfterLast('.')
    }

    return try {
        Replication.valueOf(enumName)
    } catch (exception: IllegalArgumentException) {
        fail(
            environment,
            "Invalid value for 'replication' on $context. Expected a valid Replication enum, but got '$enumName'."
        )
    }
}

internal fun extractAnnotations(
    declaration: KSClassDeclaration,
    modelAnnotation: KSAnnotation,
    frameworkDeclarations: Set<KSClassDeclaration>
): List<AnnotationModel>? {
    val directAnnotations = declaration.annotations.toAnnotationModels(frameworkDeclarations) ?: emptyList()

    val listedAnnotations =
        (modelAnnotation.arguments.find { it.name?.asString() == ANNOTATIONS_ARG }?.value as? List<*>)
            ?.mapNotNull { (it as? KSType)?.declaration?.qualifiedName?.asString() }
            ?.map { AnnotationModel(it) } ?: emptyList()

    return (directAnnotations + listedAnnotations).takeIf { it.isNotEmpty() }
}

internal fun extractOptInMarkersFromModelGen(modelAnnotation: KSAnnotation): List<String> {
    return (modelAnnotation.arguments.find { it.name?.asString() == OPT_IN_MARKERS_ARG }?.value as? List<*>)
        ?.mapNotNull { (it as? KSType)?.declaration?.qualifiedName?.asString() }
        ?: emptyList()
}

internal fun extractOptInMarkersFromProperties(declaration: KSClassDeclaration): List<String> {
    return declaration.getAllProperties()
        .flatMap { property -> property.annotations }
        .filter { annotation -> annotation.isAnnotation(OPT_IN_ANNOTATION_NAME) }
        .flatMap { optInAnnotation ->
            (optInAnnotation.arguments.first().value as? List<*>)?.mapNotNull {
                (it as? KSType)?.declaration?.qualifiedName?.asString()
            } ?: emptyList()
        }
        .toList()
}


internal data class VersioningInfo(val baseModelName: String, val schemaVersion: Int)

internal fun determineVersioningInfo(
    declaration: KSClassDeclaration,
    environment: SymbolProcessorEnvironment
): VersioningInfo? {
    val baseInterface = declaration.superTypes
        .mapNotNull { it.resolve().declaration as? KSClassDeclaration }
        .firstOrNull {
            it.classKind == ClassKind.INTERFACE && !it.annotations.any { annotation ->
                annotation.isAnnotation(
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
        environment,
        "Versioned model '${declaration.simpleName.asString()}' must either be named 'V<N>' (e.g., V1) " +
                "or be annotated with @SchemaVersion(number = N)."
    )

    return VersioningInfo(baseInterface.simpleName.asString(), version)
}

fun Sequence<KSAnnotation>.toAnnotationModels(
    frameworkDeclarations: Set<KSClassDeclaration>
): List<AnnotationModel>? =
    mapNotNull { annotation ->
        val declaration = annotation.annotationType.resolve().declaration as? KSClassDeclaration
            ?: return@mapNotNull null
        if (declaration in frameworkDeclarations) return@mapNotNull null
        val arguments = annotation.arguments.associate { argument ->
            val key = argument.name?.asString() ?: "value"
            val rawValue = argument.value
            key to when (rawValue) {
                is String -> AnnotationArgument.StringValue(rawValue)
                else -> AnnotationArgument.LiteralValue(rawValue.toString())
            }
        }
        AnnotationModel(declaration.qualifiedName!!.asString(), arguments)
    }
        .toList()
        .takeIf { it.isNotEmpty() }

internal fun isModelAnnotation(declaration: KSClassDeclaration): Boolean {
    val isHidden = declaration.annotations.any {
        it.annotationType.resolve().declaration.qualifiedName?.asString() == HIDE_ANNOTATION_NAME
    }
    return declaration.classKind == ClassKind.INTERFACE && !isHidden
}

internal fun getFrameworkDeclarations(resolver: Resolver): Set<KSClassDeclaration> {
    return listOf(MODEL_ANNOTATION_NAME, FIELD_ANNOTATION_NAME, SCHEMA_VERSION_ANNOTATION_NAME)
        .mapNotNull { fullyQualifiedName ->
            resolver.getClassDeclarationByName(resolver.getKSNameFromString(fullyQualifiedName))
        }
        .toSet()
}