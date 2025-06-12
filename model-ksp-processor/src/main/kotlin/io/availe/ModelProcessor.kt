package io.availe

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import io.availe.models.*
import kotlinx.serialization.json.Json
import java.io.OutputStreamWriter

private val MODEL_ANNOTATION_NAME = ModelGen::class.qualifiedName!!
private val FIELD_ANNOTATION_NAME = FieldGen::class.qualifiedName!!
private const val ID_PROPERTY = "id"

class ModelProcessor(private val env: SymbolProcessorEnvironment) : SymbolProcessor {
    private var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) return emptyList()

        val symbols = resolver.getSymbolsWithAnnotation(MODEL_ANNOTATION_NAME)
            .filterIsInstance<KSClassDeclaration>()
        if (!symbols.iterator().hasNext()) return emptyList()

        val models = parseModels(symbols, resolver)
        val jsonData = Json { prettyPrint = true }.encodeToString(models)

        val srcFiles = symbols.mapNotNull { it.containingFile }.toList().toTypedArray()
        val deps = Dependencies(true, *srcFiles)

        val file = env.codeGenerator.createNewFile(deps, "", "models", "json")
        OutputStreamWriter(file, "UTF-8").use { it.write(jsonData) }

        invoked = true
        return emptyList()
    }

    private fun getDiscoveredAnnotationModels(annotated: KSAnnotated): List<AnnotationModel> {
        return annotated.annotations.mapNotNull { ann ->
            val fqName = ann.annotationType.resolve().declaration.qualifiedName?.asString() ?: return@mapNotNull null
            if (fqName == MODEL_ANNOTATION_NAME || fqName == FIELD_ANNOTATION_NAME) return@mapNotNull null

            val args = ann.arguments.associate { arg ->
                val name = arg.name?.asString() ?: "value"
                val value = when (val v = arg.value) {
                    is String -> AnnotationArgument.StringValue(v)
                    else -> AnnotationArgument.LiteralValue(v.toString())
                }
                name to value
            }
            AnnotationModel(fqName, args)
        }.toList()
    }

    @Suppress("UNCHECKED_CAST")
    private fun getKClassListAnnotations(ann: KSAnnotation): List<AnnotationModel> {
        val kstypes = ann.arguments
            .firstOrNull { it.name?.asString() == "annotations" }
            ?.value as? List<KSType> ?: return emptyList()

        return kstypes.map { kstype ->
            AnnotationModel(
                qualifiedName = kstype.declaration.qualifiedName!!.asString(),
                arguments = emptyMap()
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getOptInMarkers(ann: KSAnnotation): List<String>? {
        val arg = ann.arguments.firstOrNull { it.name?.asString() == "optInMarkers" }?.value as? List<KSType>
        return arg?.map { it.declaration.qualifiedName!!.asString() }?.takeIf { it.isNotEmpty() }
    }

    private fun parseModels(symbols: Sequence<KSClassDeclaration>, resolver: Resolver): List<Model> =
        symbols.map { cls ->
            val modelAnn = cls.annotations.first { it.annotationType.resolve().declaration.qualifiedName?.asString() == MODEL_ANNOTATION_NAME }
            val name = cls.simpleName.asString()

            val classRep = modelAnn.arguments
                .firstOrNull { it.name?.asString() == "replication" }
                ?.value?.let { value ->
                    try {
                        Replication.valueOf(value.toString().substringAfterLast('.'))
                    } catch (e: Exception) {
                        null
                    }
                } ?: Replication.BOTH


            val annotationsFromParam = getKClassListAnnotations(modelAnn)
            val annotationsFromDiscovery = getDiscoveredAnnotationModels(cls)
            val modelAnnotations = (annotationsFromParam + annotationsFromDiscovery).takeIf { it.isNotEmpty() }
            val optInMarkers = getOptInMarkers(modelAnn)

            val props = cls.getAllProperties().map { prop ->
                val propName = prop.simpleName.asString()
                val rawType = prop.type.resolve()
                val isOption = rawType.declaration.qualifiedName?.asString() == "arrow.core.Option"
                val targetType = if (isOption) rawType.arguments.first().type!!.resolve() else rawType

                val targetTypeDeclaration = resolver.getClassDeclarationByName(targetType.declaration.qualifiedName!!)
                val isForeign = targetTypeDeclaration
                    ?.annotations
                    ?.any { it.annotationType.resolve().declaration.qualifiedName?.asString() == MODEL_ANNOTATION_NAME } == true

                val fieldAnn = prop.annotations.firstOrNull { it.annotationType.resolve().declaration.qualifiedName?.asString() == FIELD_ANNOTATION_NAME }
                val fieldRep = fieldAnn?.let { annotation ->
                    annotation.arguments.firstOrNull()?.value?.let { value ->
                        try {
                            Replication.valueOf(value.toString().substringAfterLast('.'))
                        } catch (e: Exception) {
                            null
                        }
                    }
                } ?: classRep

                val propAnnotationsFromParam = fieldAnn?.let { getKClassListAnnotations(it) } ?: emptyList()
                val propAnnotationsFromDiscovery = getDiscoveredAnnotationModels(prop)
                val fieldAnnotations = (propAnnotationsFromParam + propAnnotationsFromDiscovery).takeIf { it.isNotEmpty() }

                if (isForeign) {
                    val foreignClassDecl = targetTypeDeclaration!!

                    val idProperty = foreignClassDecl.getAllProperties()
                        .firstOrNull { it.simpleName.asString() == ID_PROPERTY }
                        ?: error("Foreign model '${foreignClassDecl.simpleName.asString()}' must have an 'id' property.")

                    val idRawType = idProperty.type.resolve()
                    val idIsOption = idRawType.declaration.qualifiedName?.asString() == "arrow.core.Option"
                    val idUnderlyingType = if (idIsOption) idRawType.arguments.first().type!!.resolve() else idRawType
                    val idUnderlyingTypeFqcn = idUnderlyingType.declaration.qualifiedName!!.asString()

                    Property.ForeignProperty(
                        name = propName,
                        foreignModelName = foreignClassDecl.simpleName.asString(),
                        property = Property.Property(
                            name = ID_PROPERTY,
                            underlyingType = idUnderlyingTypeFqcn,
                            replication = Replication.BOTH,
                            annotations = null
                        ),
                        replication = fieldRep,
                        annotations = fieldAnnotations
                    )
                } else {
                    Property.Property(
                        name = propName,
                        underlyingType = targetType.declaration.qualifiedName!!.asString(),
                        replication = fieldRep,
                        annotations = fieldAnnotations
                    )
                }
            }.toList()

            Model(
                name = name,
                properties = props,
                replication = classRep,
                annotations = modelAnnotations,
                optInMarkers = optInMarkers
            )
        }.toList()
}