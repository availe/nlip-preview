package io.availe

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FileSpec
import io.availe.builders.buildValueClass
import io.availe.builders.dataClassBuilder
import io.availe.models.*
import io.availe.utils.fieldsForBase
import io.availe.utils.fieldsForCreate
import io.availe.utils.fieldsForPatch
import io.availe.utils.validateModelReplications
import java.io.OutputStreamWriter

private val MODEL_ANNOTATION = ModelGen::class.qualifiedName!!
private const val FIELD_ANNOTATION = "FieldGen"
private const val TARGET_PACKAGE = "io.availe.models"
private const val IDENTIFIERS_PREFIX = "Identifiers"
private const val ID_PROPERTY = "id"

class ModelProcessor(private val env: SymbolProcessorEnvironment) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        println("ModelProcessor started")
        val codeGen = env.codeGenerator
        val symbols = resolver.getSymbolsWithAnnotation(MODEL_ANNOTATION)
            .filterIsInstance<KSClassDeclaration>()
        println("Found ${symbols.count()} symbols with @ModelGen")
        val models = mutableListOf<Model>()
        symbols.forEach { cls ->
            println("Processing class: ${cls.qualifiedName?.asString()}")
            val ann = cls.annotations.firstOrNull { it.shortName.asString() == "ModelGen" }
            if (ann == null) {
                println("No @ModelGen annotation on ${cls.simpleName.asString()}, skipping")
                return@forEach
            }
            val name = cls.simpleName.asString()
            val module = ann.arguments
                .firstOrNull { it.name?.asString() == "module" }
                ?.value as? Module
                ?: Module.SHARED
            val classRep = ann.arguments
                .firstOrNull { it.name?.asString() == "replication" }
                ?.value as? Replication
                ?: Replication.BOTH
            println("  module = $module, replication = $classRep")
            val props = cls.getAllProperties().map { prop ->
                println("  Property: ${prop.simpleName.asString()}")
                val raw = prop.type.resolve()
                val isOption = raw.declaration.qualifiedName?.asString() == "arrow.core.Option"
                val isNullable = raw.isMarkedNullable
                val optional = isOption || isNullable
                val targetDecl = if (isOption) raw.arguments.first().type!!.resolve() else raw
                val fqcn = targetDecl.declaration.qualifiedName!!.asString()
                println("    raw=${raw.declaration.qualifiedName?.asString()} isOption=$isOption isNullable=$isNullable fqcn=$fqcn")
                val fieldRep = prop.annotations
                    .firstOrNull { it.shortName.asString() == FIELD_ANNOTATION }
                    ?.arguments
                    ?.firstOrNull { it.name?.asString() == "replication" }
                    ?.value as? Replication
                    ?: classRep
                println("    fieldRep = $fieldRep")
                val isForeign = resolver
                    .getClassDeclarationByName(raw.declaration.qualifiedName!!)
                    ?.annotations
                    ?.any { it.shortName.asString() == "ModelGen" } == true
                println("    isForeign = $isForeign")
                if (isForeign) {
                    Property.ForeignProperty(
                        name = prop.simpleName.asString(),
                        property = Property.Property(
                            name = ID_PROPERTY,
                            underlyingType = "kotlin.String",
                            optional = false,
                            replication = Replication.BOTH
                        ),
                        optional = optional,
                        replication = fieldRep
                    )
                } else {
                    Property.Property(
                        name = prop.simpleName.asString(),
                        underlyingType = fqcn,
                        optional = optional,
                        replication = fieldRep
                    )
                }
            }.toList()
            println("Built ${props.size} properties for $name")
            models += Model(name, module, props, classRep)
        }
        println("Validating model replications")
        validateModelReplications(models)
        println("Validation complete")
        models.groupBy { it.module }.forEach { (mod, list) ->
            println("Generating value classes for module $mod")
            val file = codeGen.createNewFile(Dependencies(false), TARGET_PACKAGE, IDENTIFIERS_PREFIX + mod.name)
            OutputStreamWriter(file).use { w ->
                FileSpec.builder(TARGET_PACKAGE, IDENTIFIERS_PREFIX).apply {
                    list.forEach { m ->
                        m.properties.filterIsInstance<Property.Property>().forEach { p ->
                            addType(buildValueClass(m, p))
                        }
                    }
                }.build().writeTo(w)
            }
        }
        models.forEach { model ->
            println("Generating data classes for model ${model.name}")
            val file = codeGen.createNewFile(Dependencies(false), TARGET_PACKAGE, model.name)
            OutputStreamWriter(file).use { w ->
                FileSpec.builder(TARGET_PACKAGE, model.name).apply {
                    listOf(
                        fieldsForBase(model) to Variant.BASE,
                        fieldsForCreate(model) to Variant.CREATE,
                        fieldsForPatch(model) to Variant.PATCH
                    ).forEach { (fields, variant) ->
                        println("  variant=${variant.name} fields=${fields.size}")
                        if (fields.isNotEmpty()) {
                            addType(dataClassBuilder(model, fields, variant))
                        }
                    }
                }.build().writeTo(w)
            }
        }
        println("ModelProcessor finished")
        return emptyList()
    }
}
