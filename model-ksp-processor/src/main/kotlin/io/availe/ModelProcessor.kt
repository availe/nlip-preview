package io.availe

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.*
import io.availe.models.*
import kotlinx.serialization.json.Json
import java.io.OutputStreamWriter

private val MODEL_ANNOTATION_NAME = ModelGen::class.qualifiedName!!
private val SCHEMA_VERSION_ANNOTATION_NAME = SchemaVersion::class.qualifiedName!!
private val FIELD_ANNOTATION_NAME = FieldGen::class.qualifiedName!!
private val HIDE_ANNOTATION_NAME = Hide::class.qualifiedName!!
private const val SCHEMA_VERSION_PROPERTY = "schemaVersion"
private val V_INT_REGEX = Regex("^V(\\d+)$")

class ModelProcessor(private val env: SymbolProcessorEnvironment) : SymbolProcessor {
    private var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) return emptyList()

        val symbols = resolver.getSymbolsWithAnnotation(MODEL_ANNOTATION_NAME)
            .filterIsInstance<KSClassDeclaration>()
            .filterNot { classDecl ->
                classDecl.annotations.any { ann ->
                    ann.annotationType.resolve().declaration.qualifiedName?.asString() == HIDE_ANNOTATION_NAME
                }
            }

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

    private fun KSType.toTypeInfo(): TypeInfo {
        val decl = this.declaration
        val qualifiedName = decl.qualifiedName!!.asString()
        val args = this.arguments.mapNotNull { it.type?.resolve()?.toTypeInfo() }
        return TypeInfo(
            qualifiedName = qualifiedName,
            arguments = args,
            isNullable = this.isMarkedNullable
        )
    }

    private tailrec fun TypeInfo.getLeafType(): TypeInfo {
        return if (this.arguments.isEmpty()) this else this.arguments.last().getLeafType()
    }

    private fun getDiscoveredAnnotationModels(annotated: KSAnnotated): List<AnnotationModel> {
        return annotated.annotations.mapNotNull { ann ->
            val fqName = ann.annotationType.resolve().declaration.qualifiedName?.asString() ?: return@mapNotNull null
            if (setOf(MODEL_ANNOTATION_NAME, FIELD_ANNOTATION_NAME, HIDE_ANNOTATION_NAME, SCHEMA_VERSION_ANNOTATION_NAME).contains(fqName)) {
                return@mapNotNull null
            }
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
            val packageName = cls.packageName.asString()

            val baseModel = cls.superTypes
                .map { it.resolve().declaration }
                .filterIsInstance<KSClassDeclaration>()
                .firstOrNull { superDecl ->
                    superDecl.classKind == ClassKind.INTERFACE &&
                            superDecl.annotations.none { it.annotationType.resolve().declaration.qualifiedName?.asString() == MODEL_ANNOTATION_NAME }
                }

            var schemaVersion: Int? = null
            var schemaVersionProperty: Property.Property? = null

            if (baseModel != null) {
                val schemaVersionAnn = cls.annotations.firstOrNull { it.annotationType.resolve().declaration.qualifiedName?.asString() == SCHEMA_VERSION_ANNOTATION_NAME }
                if (schemaVersionAnn != null) {
                    schemaVersion = schemaVersionAnn.arguments.first { it.name?.asString() == "number" }.value as Int
                } else {
                    val match = V_INT_REGEX.find(name)
                    if (match != null) {
                        schemaVersion = match.groupValues[1].toInt()
                    } else {
                        error("Model validation failed for '$name': Versioned models must either follow the 'V<Int>' naming convention or be annotated with '@SchemaVersion(number = ...)'.")
                    }
                }
                schemaVersionProperty = Property.Property(
                    name = SCHEMA_VERSION_PROPERTY,
                    typeInfo = TypeInfo("kotlin.Int"),
                    replication = Replication.BOTH
                )
            }

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

            val baseProps = cls.getAllProperties().map { prop ->
                val propName = prop.simpleName.asString()
                val propTypeInfo = prop.type.resolve().toTypeInfo()
                val leafTypeInfo = propTypeInfo.getLeafType()

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

                Property.Property(
                    name = propName,
                    typeInfo = propTypeInfo,
                    replication = fieldRep,
                    annotations = fieldAnnotations
                )
            }.toMutableList()

            if (schemaVersionProperty != null) {
                baseProps.add(schemaVersionProperty)
            }

            Model(
                name = name,
                packageName = packageName,
                properties = baseProps,
                replication = classRep,
                annotations = modelAnnotations,
                optInMarkers = optInMarkers,
                isVersionOf = baseModel?.simpleName?.asString(),
                schemaVersion = schemaVersion
            )
        }.toList()
}
