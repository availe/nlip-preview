package io.availe

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import io.availe.models.Model
import io.availe.models.Module
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
        if (invoked) {
            return emptyList()
        }

        val symbols = resolver.getSymbolsWithAnnotation(MODEL_ANNOTATION).filterIsInstance<KSClassDeclaration>()
        if (!symbols.iterator().hasNext()) {
            return emptyList()
        }

        val models = parseModels(symbols, resolver)
        val json = Json { prettyPrint = true }
        val jsonData = json.encodeToString(models)

        val sourceFiles = symbols.mapNotNull { it.containingFile }.toList().toTypedArray()
        val dependencies = Dependencies(true, *sourceFiles)

        val file = env.codeGenerator.createNewFile(dependencies, "", "models", "json")
        OutputStreamWriter(file, "UTF-8").use { it.write(jsonData) }

        invoked = true
        return emptyList()
    }

    private fun parseModels(symbols: Sequence<KSClassDeclaration>, resolver: Resolver): List<Model> {
        return symbols.map { cls ->
            val ann = cls.annotations.first { it.shortName.asString() == "ModelGen" }
            val name = cls.simpleName.asString()
            val module = ann.arguments.firstOrNull { it.name?.asString() == "module" }?.value as? Module ?: Module.SHARED
            val classRep = ann.arguments.firstOrNull { it.name?.asString() == "replication" }?.value as? Replication ?: Replication.BOTH
            val props = cls.getAllProperties().map { prop ->
                val raw = prop.type.resolve()
                val isOption = raw.declaration.qualifiedName?.asString() == "arrow.core.Option"
                val isNullable = raw.isMarkedNullable
                val optional = isOption || isNullable
                val targetDecl = if (isOption) raw.arguments.first().type!!.resolve() else raw
                val fqcn = targetDecl.declaration.qualifiedName!!.asString()
                val simpleTypeName = targetDecl.declaration.simpleName.asString()
                val fieldRep = prop.annotations.firstOrNull { it.shortName.asString() == FIELD_ANNOTATION }?.arguments?.firstOrNull { it.name?.asString() == "replication" }?.value as? Replication ?: classRep
                val isForeign = resolver.getClassDeclarationByName(raw.declaration.qualifiedName!!)?.annotations?.any { it.shortName.asString() == "ModelGen" } == true

                if (isForeign) {
                    Property.ForeignProperty(
                        name = prop.simpleName.asString(),
                        foreignModelName = simpleTypeName,
                        property = Property.Property(name = ID_PROPERTY, underlyingType = "kotlin.Long", optional = false, replication = Replication.BOTH),
                        optional = optional,
                        replication = fieldRep
                    )
                } else {
                    Property.Property(name = prop.simpleName.asString(), underlyingType = fqcn, optional = optional, replication = fieldRep)
                }
            }.toList()
            Model(name, module, props, classRep)
        }.toList()
    }
}