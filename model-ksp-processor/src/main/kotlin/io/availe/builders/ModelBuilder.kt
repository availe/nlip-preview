package io.availe

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import io.availe.helpers.MODEL_ANNOTATION_NAME
import io.availe.helpers.SCHEMA_VERSION_FIELD
import io.availe.models.Model
import io.availe.models.Property

internal fun buildModel(
    declaration: KSClassDeclaration,
    resolver: Resolver,
    frameworkDeclarations: Set<KSClassDeclaration>,
    environment: SymbolProcessorEnvironment
): Model {
    val modelAnnotation = declaration.annotations.first { it.isAnnotation(MODEL_ANNOTATION_NAME) }
    val modelReplication =
        extractReplication(modelAnnotation, "model '${declaration.simpleName.asString()}'", environment)
    val versioningInfo = determineVersioningInfo(declaration, environment)

    val properties = declaration.getAllProperties().map { property ->
        processProperty(property, modelReplication, resolver, frameworkDeclarations, environment)
    }.toMutableList()

    if (versioningInfo != null) {
        val schemaVersionProperty = Property.Property(
            name = SCHEMA_VERSION_FIELD,
            typeInfo = io.availe.models.TypeInfo("kotlin.Int", isNullable = false),
            replication = modelReplication
        )
        properties.add(schemaVersionProperty)
    }

    val optInMarkersFromModelGen = extractOptInMarkersFromModelGen(modelAnnotation)
    val optInMarkersFromProperties = extractOptInMarkersFromProperties(declaration)
    val allOptInMarkers = (optInMarkersFromModelGen + optInMarkersFromProperties).distinct().takeIf { it.isNotEmpty() }

    return Model(
        name = declaration.simpleName.asString(),
        packageName = declaration.packageName.asString(),
        properties = properties,
        replication = modelReplication,
        annotations = extractAnnotations(declaration, modelAnnotation, frameworkDeclarations),
        optInMarkers = allOptInMarkers,
        isVersionOf = versioningInfo?.baseModelName,
        schemaVersion = versioningInfo?.schemaVersion
    )
}