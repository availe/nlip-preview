package io.availe

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import io.availe.models.Model
import io.availe.models.Property
import io.availe.models.Replication
import kotlinx.serialization.json.Json
import java.io.OutputStreamWriter

private val MODEL_ANNOTATION = ModelGen::class.qualifiedName!!
private const val FIELD_ANNOTATION = "FieldGen"
private const val ID_PROPERTY = "id"

class ModelProcessor(private val env: SymbolProcessorEnvironment) : SymbolProcessor {
    private var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) return emptyList()

        val symbols = resolver.getSymbolsWithAnnotation(MODEL_ANNOTATION)
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

    @Suppress("UNCHECKED_CAST")
    private fun KSAnnotation.getKClassListArgument(name: String): List<String>? {
        val arg = this.arguments.firstOrNull { it.name?.asString() == name }?.value as? List<KSType>
        return arg?.map { it.declaration.qualifiedName!!.asString() }?.takeIf { it.isNotEmpty() }
    }

    private fun parseModels(symbols: Sequence<KSClassDeclaration>, resolver: Resolver): List<Model> =
        symbols.map { cls ->
            val modelAnn = cls.annotations.first { it.shortName.asString() == "ModelGen" }
            val name = cls.simpleName.asString()
            val classRep = modelAnn.arguments.firstOrNull { it.name?.asString() == "replication" }?.value as? Replication ?: Replication.BOTH
            val modelAnnotations = modelAnn.getKClassListArgument("annotations")
            val optInMarkers = modelAnn.getKClassListArgument("optInMarkers")

            val props = cls.getAllProperties().map { prop ->
                val rawType = prop.type.resolve()
                val isOption = rawType.declaration.qualifiedName?.asString() == "arrow.core.Option"
                val targetType = if (isOption) rawType.arguments.first().type!!.resolve() else rawType
                val fqcn = targetType.declaration.qualifiedName!!.asString()
                val shortName = targetType.declaration.simpleName.asString()

                val fieldAnn = prop.annotations.firstOrNull { it.shortName.asString() == FIELD_ANNOTATION }
                val fieldRep = fieldAnn?.arguments?.firstOrNull { it.name?.asString() == "replication" }?.value as? Replication ?: classRep
                val fieldAnnotations = fieldAnn?.getKClassListArgument("annotations")

                val isForeign = resolver
                    .getClassDeclarationByName(targetType.declaration.qualifiedName!!)
                    ?.annotations?.any { it.shortName.asString() == "ModelGen" } == true

                if (isForeign) {
                    Property.ForeignProperty(
                        name = prop.simpleName.asString(),
                        foreignModelName = shortName,
                        property = Property.Property(
                            name = ID_PROPERTY,
                            underlyingType = "kotlin.Long",
                            replication = Replication.BOTH,
                            annotations = null
                        ),
                        replication = fieldRep,
                        annotations = fieldAnnotations
                    )
                } else {
                    Property.Property(
                        name = prop.simpleName.asString(),
                        underlyingType = fqcn,
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