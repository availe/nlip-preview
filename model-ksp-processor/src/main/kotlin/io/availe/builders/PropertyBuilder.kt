package io.availe.builders

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import io.availe.extractReplication
import io.availe.helpers.FIELD_ANNOTATION_NAME
import io.availe.helpers.KSTypeInfo
import io.availe.helpers.MODEL_ANNOTATION_NAME
import io.availe.helpers.toModelTypeInfo
import io.availe.isAnnotation
import io.availe.models.AnnotationModel
import io.availe.models.Property
import io.availe.models.Replication
import io.availe.models.TypeInfo
import io.availe.toAnnotationModels

internal fun processProperty(
    propertyDeclaration: KSPropertyDeclaration,
    modelReplication: Replication,
    resolver: Resolver,
    frameworkDeclarations: Set<KSClassDeclaration>,
    environment: SymbolProcessorEnvironment
): Property {
    val fieldAnnotation = propertyDeclaration.annotations.firstOrNull { it.isAnnotation(FIELD_ANNOTATION_NAME) }
    val propertyReplication = fieldAnnotation?.let { ann ->
        extractReplication(ann, "property '${propertyDeclaration.simpleName.asString()}'", environment)
    } ?: modelReplication

    val ksTypeInfo = KSTypeInfo.from(propertyDeclaration.type.resolve())
    val typeInfo: TypeInfo = ksTypeInfo.toModelTypeInfo()
    val propertyAnnotations: List<AnnotationModel>? =
        propertyDeclaration.annotations.toAnnotationModels(frameworkDeclarations)

    val foreignDecl = resolver.getClassDeclarationByName(
        resolver.getKSNameFromString(ksTypeInfo.leafType.qualifiedName)
    )
    val isForeignModel = foreignDecl?.annotations?.any { it.isAnnotation(MODEL_ANNOTATION_NAME) } == true

    println(
        "processProperty name=${propertyDeclaration.simpleName.asString()} " +
                "qualified=${typeInfo.qualifiedName} isValueClass=${typeInfo.isValueClass} foreign=$isForeignModel"
    )

    return if (isForeignModel) {
        createForeignProperty(
            propertyDeclaration,
            typeInfo,
            foreignDecl,
            propertyReplication,
            propertyAnnotations
        )
    } else {
        Property.Property(
            name = propertyDeclaration.simpleName.asString(),
            typeInfo = typeInfo,
            replication = propertyReplication,
            annotations = propertyAnnotations
        )
    }
}

private fun createForeignProperty(
    propertyDeclaration: KSPropertyDeclaration,
    typeInformation: TypeInfo,
    foreignModelDeclaration: KSClassDeclaration,
    replication: Replication,
    annotations: List<AnnotationModel>?
): Property.ForeignProperty {
    return Property.ForeignProperty(
        name = propertyDeclaration.simpleName.asString(),
        typeInfo = typeInformation,
        foreignModelName = foreignModelDeclaration.simpleName.asString(),
        replication = replication,
        annotations = annotations
    )
}