package io.availe

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import io.availe.helpers.MODEL_ANNOTATION_NAME
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
            .filter(::isModelAnnotation)
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

    private fun writeModelsToFile(models: List<Model>, sourceSymbols: List<KSClassDeclaration>) {
        val jsonText = Json { prettyPrint = true }.encodeToString(models)
        val sourceFiles = sourceSymbols.mapNotNull { it.containingFile }.toTypedArray()
        val dependencies = Dependencies(true, *sourceFiles)
        val file = env.codeGenerator.createNewFile(dependencies, "", "models", "json")

        OutputStreamWriter(file, "UTF-8").use { it.write(jsonText) }
    }
}