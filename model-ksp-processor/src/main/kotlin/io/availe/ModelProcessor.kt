package io.availe

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FileSpec
import io.availe.builders.buildGenericInterface
import io.availe.builders.buildValueClass
import io.availe.builders.dataClassBuilder
import io.availe.builders.packageName
import io.availe.models.*
import io.availe.utils.fieldsForBase
import io.availe.utils.fieldsForCreate
import io.availe.utils.fieldsForPatch
import io.availe.utils.validateModelReplications
import java.io.OutputStreamWriter

private val MODEL_ANNOTATION = ModelGen::class.qualifiedName!!
private const val FIELD_ANNOTATION = "FieldGen"
private const val ID_PROPERTY = "id"

class ModelProcessor(private val env: SymbolProcessorEnvironment) : SymbolProcessor {
    private var isInitialRound = true

    private fun parseModels(symbols: Sequence<KSClassDeclaration>, resolver: Resolver): List<Model> {
        val models = mutableListOf<Model>()
        symbols.forEach { cls ->
            val ann = cls.annotations.firstOrNull { it.shortName.asString() == "ModelGen" } ?: return@forEach
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
                val fieldRep = prop.annotations.firstOrNull { it.shortName.asString() == FIELD_ANNOTATION }?.arguments?.firstOrNull { it.name?.asString() == "replication" }?.value as? Replication ?: classRep
                val isForeign = resolver.getClassDeclarationByName(raw.declaration.qualifiedName!!)?.annotations?.any { it.shortName.asString() == "ModelGen" } == true

                if (isForeign) {
                    Property.ForeignProperty(name = prop.simpleName.asString(), property = Property.Property(name = ID_PROPERTY, underlyingType = "kotlin.String", optional = false, replication = Replication.BOTH), optional = optional, replication = fieldRep)
                } else {
                    Property.Property(name = prop.simpleName.asString(), underlyingType = fqcn, optional = optional, replication = fieldRep)
                }
            }.toList()
            models += Model(name, module, props, classRep)
        }
        return models
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(MODEL_ANNOTATION).filterIsInstance<KSClassDeclaration>()
        if (!symbols.iterator().hasNext()) {
            return emptyList()
        }

        if (isInitialRound) {
            isInitialRound = false
            println("KSP Round 1: Generating foundational interfaces and identifiers...")
            val models = parseModels(symbols, resolver)
            validateModelReplications(models)

            models.forEach { model ->
                val interfaceFile = FileSpec.builder(packageName, "I${model.name}")
                    .addType(buildGenericInterface(model))
                    .build()
                val file = env.codeGenerator.createNewFile(Dependencies(false), packageName, "I${model.name}")
                OutputStreamWriter(file).use { interfaceFile.writeTo(it) }
            }

            val modelsByModule = models.groupBy { it.module }
            modelsByModule.forEach { (module, moduleModels) ->
                val identifierFileSpec = FileSpec.builder(packageName, "Identifiers${module.name}")
                moduleModels.flatMap { it.properties }.filterIsInstance<Property.Property>().forEach { prop ->
                    val model = models.first { m -> m.properties.contains(prop) }
                    identifierFileSpec.addType(buildValueClass(model, prop))
                }
                val file = env.codeGenerator.createNewFile(Dependencies(false), packageName, "Identifiers${module.name}")
                OutputStreamWriter(file).use { identifierFileSpec.build().writeTo(it) }
            }

            return symbols.toList()
        }

        println("KSP Round 2: Generating DTOs...")
        val models = parseModels(symbols, resolver)
        models.forEach { model ->
            val fileBuilder = FileSpec.builder(packageName, model.name)
            listOf(
                fieldsForBase(model) to Variant.BASE,
                fieldsForCreate(model) to Variant.CREATE,
                fieldsForPatch(model) to Variant.PATCH,
            ).forEach { (fields, variant) ->
                if (fields.isNotEmpty()) {
                    fileBuilder.addType(dataClassBuilder(model, fields, variant))
                }
            }
            val file = env.codeGenerator.createNewFile(Dependencies(false), packageName, model.name)
            OutputStreamWriter(file).use { fileBuilder.build().writeTo(it) }
        }

        return emptyList()
    }
}