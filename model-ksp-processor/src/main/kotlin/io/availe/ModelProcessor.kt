package io.availe

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.typeNameOf
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

class ModelProcessor(
    private val env: SymbolProcessorEnvironment
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val codeGen = env.codeGenerator
        val symbols = resolver.getSymbolsWithAnnotation(MODEL_ANNOTATION)
            .filterIsInstance<KSClassDeclaration>()

        if (symbols.none()) {
            env.logger.warn("No symbols with @ModelGen were found by KSP")
        } else {
            env.logger.warn("Found @ModelGen symbols:")
            symbols.forEach {
                env.logger.warn(" - ${it.qualifiedName?.asString()}")
            }
        }

        val models = mutableListOf<Model>()
        symbols.forEach { cls ->
            val name = cls.simpleName.asString()
            val ann = cls.annotations.first { it.shortName.asString() == "ModelGen" }
            val module = ann.arguments.first { it.name?.asString() == "module" }.value as Module
            val classRep = ann.arguments.first { it.name?.asString() == "replication" }.value as Replication
            val props: List<Property> = cls.getAllProperties()
                .map { prop ->
                    val raw = prop.type.resolve()
                    val baseType = raw.toTypeName()
                    val isOption = raw.declaration.qualifiedName?.asString() == "arrow.core.Option"
                    val isNullable = raw.isMarkedNullable
                    val optional = isOption || isNullable
                    val underlying = if (isOption) raw.arguments.first().type!!.resolve().toTypeName() else baseType
                    val fieldRep = prop.annotations
                        .firstOrNull { it.shortName.asString() == FIELD_ANNOTATION }
                        ?.arguments?.first { it.name?.asString() == "replication" }
                        ?.value as? Replication ?: classRep
                    val isForeign = resolver
                        .getClassDeclarationByName(raw.declaration.qualifiedName!!)
                        ?.annotations
                        ?.any { it.shortName.asString() == "ModelGen" } == true
                    if (isForeign) {
                        Property.ForeignProperty(
                            name = prop.simpleName.asString(),
                            property = Property.Property(
                                name = ID_PROPERTY,
                                underlyingType = typeNameOf<String>(),
                                optional = false,
                                replication = Replication.BOTH
                            ),
                            optional = optional,
                            replication = fieldRep
                        )
                    } else {
                        Property.Property(
                            name = prop.simpleName.asString(),
                            underlyingType = underlying,
                            optional = optional,
                            replication = fieldRep
                        )
                    }
                }
                .toList()
            models += Model(name, module, props, classRep)
        }
        validateModelReplications(models)
        models.groupBy { it.module }.forEach { (mod, list) ->
            val file = codeGen.createNewFile(Dependencies(false), TARGET_PACKAGE, IDENTIFIERS_PREFIX + mod.name)
            OutputStreamWriter(file).use { w ->
                FileSpec.builder(TARGET_PACKAGE, IDENTIFIERS_PREFIX)
                    .apply {
                        list.forEach { m ->
                            m.properties.filterIsInstance<Property.Property>()
                                .forEach { p -> addType(buildValueClass(m, p)) }
                        }
                    }
                    .build()
                    .writeTo(w)
            }
        }
        models.forEach { model ->
            val file = codeGen.createNewFile(Dependencies(false), TARGET_PACKAGE, model.name)
            OutputStreamWriter(file).use { w ->
                FileSpec.builder(TARGET_PACKAGE, model.name)
                    .apply {
                        listOf(
                            fieldsForBase(model) to Variant.BASE,
                            fieldsForCreate(model) to Variant.CREATE,
                            fieldsForPatch(model) to Variant.PATCH
                        ).forEach { (fields, variant) ->
                            if (fields.isNotEmpty()) addType(
                                dataClassBuilder(
                                    model,
                                    fields,
                                    variant
                                )
                            )
                        }
                    }
                    .build()
                    .writeTo(w)
            }
        }
        return emptyList()
    }
}
