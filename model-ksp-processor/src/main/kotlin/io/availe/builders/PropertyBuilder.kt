package io.availe

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import io.availe.helpers.*
import io.availe.models.AnnotationModel
import io.availe.models.Property
import io.availe.models.Replication
import io.availe.models.TypeInfo

internal fun processProperty(
    prop: KSPropertyDeclaration,
    modelReplication: Replication,
    resolver: Resolver,
    frameworkDecls: Set<KSClassDeclaration>,
    env: SymbolProcessorEnvironment
): Property {
    val fieldAnn = prop.annotations.firstOrNull { it.isAnnotation(FIELD_ANNOTATION_NAME) }
    val propReplication = fieldAnn?.let {
        extractReplication(it, "property '${prop.simpleName.asString()}'", env)
    } ?: modelReplication

    val ksTypeInfo = KSTypeInfo.from(prop.type.resolve())
    val typeInfo = ksTypeInfo.toModelTypeInfo()
    val propAnnotations = prop.annotations.toAnnotationModels(frameworkDecls)

    val foreignModelDecl =
        resolver.getClassDeclarationByName(resolver.getKSNameFromString(ksTypeInfo.leafType.qualifiedName))
    val isForeignModel = foreignModelDecl?.annotations?.any { it.isAnnotation(MODEL_ANNOTATION_NAME) } == true

    return if (isForeignModel) {
        createForeignProperty(prop, typeInfo, foreignModelDecl!!, propReplication, propAnnotations, env)
    } else {
        Property.Property(
            name = prop.simpleName.asString(),
            typeInfo = typeInfo,
            replication = propReplication,
            annotations = propAnnotations
        )
    }
}

private fun createForeignProperty(
    prop: KSPropertyDeclaration,
    typeInfo: TypeInfo,
    foreignModelDecl: KSClassDeclaration,
    replication: Replication,
    annotations: List<AnnotationModel>?,
    env: SymbolProcessorEnvironment
): Property.ForeignProperty {
    val idProp = foreignModelDecl.getAllProperties().firstOrNull { it.simpleName.asString() == ID_PROPERTY }
        ?: fail(env, "Foreign model '${foreignModelDecl.simpleName.asString()}' must contain an 'id' property.")
    val idTypeInfo = KSTypeInfo.from(idProp.type.resolve()).toModelTypeInfo()

    return Property.ForeignProperty(
        name = prop.simpleName.asString(),
        typeInfo = typeInfo,
        foreignModelName = foreignModelDecl.simpleName.asString(),
        property = Property.Property(
            name = ID_PROPERTY,
            typeInfo = idTypeInfo,
            replication = Replication.BOTH
        ),
        replication = replication,
        annotations = annotations
    )
}