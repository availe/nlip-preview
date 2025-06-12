package io.availe.generators

import com.squareup.kotlinpoet.*
import io.availe.builders.buildValueClass
import io.availe.builders.dataClassBuilder
import io.availe.builders.packageName
import io.availe.models.Model
import io.availe.models.Property
import io.availe.models.Variant
import io.availe.utils.fieldsForBase
import io.availe.utils.fieldsForCreate
import io.availe.utils.fieldsForPatch
import java.io.File

val filePath: File = File("build/generated-src/kotlin-poet")

private fun generateVersionedModelFile(baseName: String, versions: List<Model>, out: File) {
    val sortedVersions = versions.sortedByDescending { it.schemaVersion }
    val fileBuilder = FileSpec.builder(packageName, baseName)

    val topLevelClass = TypeSpec.classBuilder(baseName)
        .addModifiers(KModifier.SEALED)
        .addAnnotation(ClassName("kotlinx.serialization", "Serializable"))

    sortedVersions.forEach { version ->
        val versionClass = TypeSpec.classBuilder(version.name)
            .addModifiers(KModifier.SEALED)
            .superclass(ClassName(packageName, baseName))
            .addAnnotation(ClassName("kotlinx.serialization", "Serializable"))

        val dtoSpecs = listOf(
            fieldsForBase(version) to Variant.BASE,
            fieldsForCreate(version) to Variant.CREATE,
            fieldsForPatch(version) to Variant.PATCH
        ).mapNotNull { (fields, variant) ->
            if (fields.isNotEmpty()) dataClassBuilder(version, fields, variant) else null
        }
        dtoSpecs.forEach { versionClass.addType(it) }

        val valueClassSpecs = version.properties
            .filterIsInstance<Property.Property>()
            .filter { it.name != "schemaVersion" }
            .map { buildValueClass(version, it, isVersioned = true) }
        valueClassSpecs.forEach { versionClass.addType(it) }

        topLevelClass.addType(versionClass.build())
    }

    fileBuilder.addType(topLevelClass.build())
    fileBuilder.build().writeTo(out)
}

fun generateDataClasses(models: List<Model>) {
    val out: File = filePath
    val groupedModels = models.groupBy { it.isVersionOf ?: it.name }

    groupedModels.forEach { (baseName, modelVersions) ->
        val isVersioned = modelVersions.first().isVersionOf != null
        if (isVersioned) {
            generateVersionedModelFile(baseName, modelVersions, out)
        } else {
            generateStandaloneModel(modelVersions.first(), out)
        }
    }
}

private fun generateStandaloneModel(model: Model, out: File) {
    val fileBuilder = FileSpec.builder(packageName, model.name)
    model.optInMarkers?.forEach { marker ->
        val markerClass = ClassName(marker.substringBeforeLast('.'), marker.substringAfterLast('.'))
        fileBuilder.addAnnotation(
            AnnotationSpec.builder(ClassName("kotlin", "OptIn"))
                .addMember("%T::class", markerClass)
                .build()
        )
    }

    val dtoSpecs = listOf(
        fieldsForBase(model) to Variant.BASE,
        fieldsForCreate(model) to Variant.CREATE,
        fieldsForPatch(model) to Variant.PATCH
    ).mapNotNull { (fields, variant) ->
        if (fields.isNotEmpty()) dataClassBuilder(model, fields, variant) else null
    }

    dtoSpecs.forEach { fileBuilder.addType(it) }

    val valueClassSpecs = model.properties
        .filterIsInstance<Property.Property>()
        .map { buildValueClass(model, it, isVersioned = false) }
    valueClassSpecs.forEach { fileBuilder.addType(it) }

    fileBuilder.build().writeTo(out)
}