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
    frameworkDecls: Set<KSClassDeclaration>,
    env: SymbolProcessorEnvironment
): Model {
    val modelAnn = declaration.annotations.first { it.isAnnotation(MODEL_ANNOTATION_NAME) }
    val modelReplication = extractReplication(modelAnn, "model '${declaration.simpleName.asString()}'", env)
    val versioningInfo = determineVersioningInfo(declaration, env)

    val properties = declaration.getAllProperties().map { prop ->
        processProperty(prop, modelReplication, resolver, frameworkDecls, env)
    }.toMutableList()

    if (versioningInfo != null) {
        val schemaVersionProperty = Property.Property(
            name = SCHEMA_VERSION_FIELD,
            typeInfo = io.availe.models.TypeInfo("kotlin.Int", isNullable = false),
            replication = modelReplication
        )
        properties.add(schemaVersionProperty)
    }

    return Model(
        name = declaration.simpleName.asString(),
        packageName = declaration.packageName.asString(),
        properties = properties,
        replication = modelReplication,
        annotations = extractAnnotations(declaration, modelAnn, frameworkDecls),
        optInMarkers = extractOptInMarkers(modelAnn),
        isVersionOf = versioningInfo?.baseModelName,
        schemaVersion = versioningInfo?.schemaVersion
    )
}