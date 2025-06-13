package io.availe

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.*
import io.availe.helpers.*
import io.availe.models.AnnotationModel
import io.availe.models.Model
import io.availe.models.Property
import io.availe.models.Replication
import kotlinx.serialization.json.Json
import java.io.OutputStreamWriter

private val V_INT_REGEX = Regex("^V(\\d+)\$")

class ModelProcessor(private val env: SymbolProcessorEnvironment) : SymbolProcessor {
    private var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) return emptyList()

        val modelSymbols = resolver
            .getSymbolsWithAnnotation(MODEL_ANNOTATION_NAME)
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.INTERFACE }
            .filterNot { cd ->
                cd.annotations.any { ann ->
                    ann.annotationType.resolve().declaration.qualifiedName?.asString() == HIDE_ANNOTATION_NAME
                }
            }
            .toList()

        if (modelSymbols.isEmpty()) return emptyList()

        val models = modelSymbols.map { decl ->
            buildModel(decl, resolver)
        }

        val jsonText = Json { prettyPrint = true }.encodeToString(models)
        val srcFiles = modelSymbols.mapNotNull { it.containingFile }.toTypedArray()
        val deps = Dependencies(true, *srcFiles)
        val file = env.codeGenerator.createNewFile(deps, "", "models", "json")

        OutputStreamWriter(file, "UTF-8").use { it.write(jsonText) }
        invoked = true

        return emptyList()
    }

    private fun buildModel(declaration: KSClassDeclaration, resolver: Resolver): Model {
        val modelAnn = declaration.annotations.first {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == MODEL_ANNOTATION_NAME
        }

        val modelReplication: Replication = modelAnn.arguments
            .firstOrNull { it.name?.asString() == REPLICATION_ARG }
            ?.value
            ?.let { rawValue ->
                when (rawValue) {
                    is KSType -> Replication.valueOf(rawValue.declaration.simpleName.asString())
                    is KSClassDeclaration -> Replication.valueOf(rawValue.simpleName.asString())
                    is KSName -> Replication.valueOf(rawValue.asString().substringAfterLast('.'))
                    else -> {
                        env.logger.error("KSP Error: Invalid replication value for ${declaration.qualifiedName?.asString()}. Raw value: $rawValue (type: ${rawValue::class.qualifiedName}). Defaulting to BOTH.")
                        Replication.BOTH
                    }
                }
            } ?: Replication.BOTH

        val annotationModelsListedInModelGen = ((modelAnn.arguments
            .firstOrNull { it.name?.asString() == ANNOTATIONS_ARG }?.value) as? List<*>)
            ?.mapNotNull { (it as? KSType)?.declaration?.qualifiedName?.asString() }
            ?.map { fq -> AnnotationModel(fq, emptyMap()) }
            ?: emptyList()

        val listedOptInMarkerNames = ((modelAnn.arguments
            .firstOrNull { it.name?.asString() == OPT_IN_MARKERS_ARG }?.value) as? List<*>)
            ?.mapNotNull { (it as? KSType)?.declaration?.qualifiedName?.asString() }
            ?: emptyList()

        val frameworkDecls = listOf(
            MODEL_ANNOTATION_NAME,
            FIELD_ANNOTATION_NAME,
            SCHEMA_VERSION_ANNOTATION_NAME
        )
            .mapNotNull { fq ->
                resolver.getClassDeclarationByName(resolver.getKSNameFromString(fq))
            }
            .toSet()

        val annotationModelsDirectOnInterface = declaration.annotations.toAnnotationModels(frameworkDecls)

        val allModelAnnotations = (
                annotationModelsListedInModelGen +
                        (annotationModelsDirectOnInterface ?: emptyList())
                ).takeIf { it.isNotEmpty() }

        val optInMarkers = listedOptInMarkerNames.takeIf { it.isNotEmpty() }

        val baseInterface = declaration.superTypes
            .mapNotNull { it.resolve().declaration as? KSClassDeclaration }
            .firstOrNull { iface ->
                iface.classKind == ClassKind.INTERFACE &&
                        iface.annotations.none {
                            it.annotationType.resolve().declaration.qualifiedName?.asString() == MODEL_ANNOTATION_NAME
                        }
            }

        val (schemaVersion, schemaVersionProperty) = if (baseInterface != null) {
            val explicit = declaration.annotations
                .firstOrNull {
                    it.annotationType.resolve().declaration.qualifiedName?.asString() == SCHEMA_VERSION_ANNOTATION_NAME
                }
                ?.arguments
                ?.firstOrNull { it.name?.asString() == SCHEMA_VERSION_ARG }
                ?.value as? Int

            val inferred = explicit
                ?: V_INT_REGEX.find(declaration.simpleName.asString())
                    ?.groupValues?.get(1)?.toIntOrNull()
                ?: error("Versioned model ${declaration.simpleName}: must use @SchemaVersion or V<n>")

            inferred to Property.Property(
                name = SCHEMA_VERSION_FIELD,
                typeInfo = io.availe.models.TypeInfo("kotlin.Int", emptyList(), false),
                replication = modelReplication
            )
        } else null to null

        val properties = declaration.getAllProperties().map { prop ->
            val kspType = KSTypeInfo.from(prop.type.resolve())
            val typeInfo = kspType.toModelTypeInfo()
            val leafName = kspType.leafType.qualifiedName

            val fieldReplication = prop.annotations
                .firstOrNull {
                    it.annotationType.resolve().declaration.qualifiedName?.asString() == FIELD_ANNOTATION_NAME
                }
                ?.arguments
                ?.firstOrNull { it.name?.asString() == REPLICATION_ARG }
                ?.value?.let { value ->
                    when (value) {
                        is KSType -> Replication.valueOf(value.declaration.simpleName.asString())
                        is KSClassDeclaration -> Replication.valueOf(value.simpleName.asString())
                        is KSName -> Replication.valueOf(value.asString().substringAfterLast('.'))
                        else -> {
                            env.logger.error("KSP Error: Invalid field replication value for ${prop.simpleName.asString()}. Raw value: $value (type: ${value::class.qualifiedName}). Defaulting to BOTH.")
                            Replication.BOTH
                        }
                    }
                } ?: modelReplication

            val fieldAnnotations = prop.annotations.toAnnotationModels(frameworkDecls)

            val foreign = resolver.getClassDeclarationByName(resolver.getKSNameFromString(leafName))

            if (foreign != null && foreign.annotations.any {
                    it.annotationType.resolve().declaration.qualifiedName?.asString() == MODEL_ANNOTATION_NAME
                }) {
                val idProp = foreign.getAllProperties().first { it.simpleName.asString() == ID_PROPERTY }
                val idType = KSTypeInfo.from(idProp.type.resolve()).toModelTypeInfo()

                Property.ForeignProperty(
                    name = prop.simpleName.asString(),
                    typeInfo = typeInfo,
                    foreignModelName = foreign.simpleName.asString(),
                    property = Property.Property(
                        name = ID_PROPERTY,
                        typeInfo = idType,
                        replication = Replication.BOTH
                    ),
                    replication = fieldReplication,
                    annotations = fieldAnnotations
                )
            } else {
                Property.Property(
                    name = prop.simpleName.asString(),
                    typeInfo = typeInfo,
                    replication = fieldReplication,
                    annotations = fieldAnnotations
                )
            }
        }
            .toMutableList()
            .apply { schemaVersionProperty?.let(::add) }

        return Model(
            name = declaration.simpleName.asString(),
            packageName = declaration.packageName.asString(),
            properties = properties,
            replication = modelReplication,
            annotations = allModelAnnotations,
            optInMarkers = optInMarkers,
            isVersionOf = baseInterface?.simpleName?.asString(),
            schemaVersion = schemaVersion
        )
    }
}