package io.availe

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import io.availe.helpers.FIELD_ANNOTATION_NAME
import io.availe.helpers.HIDE_ANNOTATION_NAME
import io.availe.helpers.MODEL_ANNOTATION_NAME
import io.availe.helpers.SCHEMA_VERSION_ANNOTATION_NAME
import io.availe.models.Model
import kotlinx.serialization.json.Json
import java.io.OutputStreamWriter

class ModelProcessor(private val env: SymbolProcessorEnvironment) : SymbolProcessor {
    private var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) return emptyList()

        val modelSymbols = resolver
            .getSymbolsWithAnnotation(MODEL_ANNOTATION_NAME)
            .filterIsInstance<KSClassDeclaration>()
            .filter(::isModelInterface)
            .toList()

        if (modelSymbols.isEmpty()) return emptyList()

        val frameworkDecls = getFrameworkDeclarations(resolver)
        val models = modelSymbols.map { decl ->
            buildModel(decl, resolver, frameworkDecls, env)
        }

        writeModelsToFile(models, modelSymbols)
        invoked = true
        return emptyList()
    }

    private fun isModelInterface(declaration: KSClassDeclaration): Boolean {
        val isHidden = declaration.annotations.any {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == HIDE_ANNOTATION_NAME
        }
        return declaration.classKind == ClassKind.INTERFACE && !isHidden
    }

    private fun getFrameworkDeclarations(resolver: Resolver): Set<KSClassDeclaration> {
        return listOf(MODEL_ANNOTATION_NAME, FIELD_ANNOTATION_NAME, SCHEMA_VERSION_ANNOTATION_NAME)
            .mapNotNull { fqName ->
                resolver.getClassDeclarationByName(resolver.getKSNameFromString(fqName))
            }
            .toSet()
    }

    private fun writeModelsToFile(models: List<Model>, sourceSymbols: List<KSClassDeclaration>) {
        val jsonText = Json { prettyPrint = true }.encodeToString(models)
        val sourceFiles = sourceSymbols.mapNotNull { it.containingFile }.toTypedArray()
        val dependencies = Dependencies(true, *sourceFiles)
        val file = env.codeGenerator.createNewFile(dependencies, "", "models", "json")

        OutputStreamWriter(file, "UTF-8").use { it.write(jsonText) }
    }
}