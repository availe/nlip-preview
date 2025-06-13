package io.availe

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import io.availe.helpers.FIELD_ANNOTATION_NAME
import io.availe.helpers.KSTypeInfo
import io.availe.helpers.MODEL_ANNOTATION_NAME
import io.availe.helpers.toModelTypeInfo
import io.availe.models.AnnotationModel
import io.availe.models.Property
import io.availe.models.Replication
import io.availe.models.TypeInfo

internal fun processProperty(
    propertyDeclaration: KSPropertyDeclaration,
    modelReplication: Replication,
    resolver: Resolver,
    frameworkDeclarations: Set<KSClassDeclaration>,
    environment: SymbolProcessorEnvironment
): Property {
    val fieldAnnotation = propertyDeclaration.annotations.firstOrNull { it.isAnnotation(FIELD_ANNOTATION_NAME) }
    val propertyReplication = fieldAnnotation?.let { annotation ->
        extractReplication(annotation, "property '${propertyDeclaration.simpleName.asString()}'", environment)
    } ?: modelReplication

    val ksTypeInformation = KSTypeInfo.from(propertyDeclaration.type.resolve())
    val typeInformation = ksTypeInformation.toModelTypeInfo()
    val propertyAnnotations = propertyDeclaration.annotations.toAnnotationModels(frameworkDeclarations)

    val foreignModelDeclaration =
        resolver.getClassDeclarationByName(resolver.getKSNameFromString(ksTypeInformation.leafType.qualifiedName))
    val isForeignModel = foreignModelDeclaration?.annotations?.any { it.isAnnotation(MODEL_ANNOTATION_NAME) } == true

    return if (isForeignModel) {
        createForeignProperty(
            propertyDeclaration,
            typeInformation,
            foreignModelDeclaration,
            propertyReplication,
            propertyAnnotations
        )
    } else {
        Property.Property(
            name = propertyDeclaration.simpleName.asString(),
            typeInfo = typeInformation,
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