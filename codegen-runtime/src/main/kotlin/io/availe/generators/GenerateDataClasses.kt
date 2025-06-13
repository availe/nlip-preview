package io.availe.generators

import com.squareup.kotlinpoet.*
import io.availe.builders.*
import io.availe.models.Model
import io.availe.models.Property
import io.availe.models.Variant
import io.availe.utils.fieldsForBase
import io.availe.utils.fieldsForCreate
import io.availe.utils.fieldsForPatch
import java.io.File

val filePath: File = File("build/generated-src/kotlin-poet")

fun generateDataClasses(models: List<Model>) {
    val outputDirectory = filePath
    val groupedModels = models.groupBy { it.isVersionOf ?: it.name }

    groupedModels.forEach { (baseName, modelVersions) ->
        val isVersioned = modelVersions.first().isVersionOf != null
        if (isVersioned) {
            generateVersionedModelFile(baseName, modelVersions, outputDirectory)
        } else {
            generateStandaloneModel(modelVersions.first(), outputDirectory)
        }
    }
}

private fun generateVersionedModelFile(baseName: String, versions: List<Model>, outputDirectory: File) {
    val sortedVersions = versions.sortedByDescending { it.schemaVersion }
    val generatedClassName = baseName + "Schema"
    val fileBuilder = FileSpec.builder(sortedVersions.first().packageName, generatedClassName)
    fileBuilder
        .addFileComment(FILE_HEADER_COMMENT)
        .addOptInMarkersForModels(versions)

    val propertySignatures = mutableMapOf<String, String>()
    val conflictingProperties = mutableSetOf<String>()

    versions.forEach { version ->
        version.properties.filterIsInstance<Property.Property>().forEach { property ->
            if (conflictingProperties.contains(property.name)) return@forEach
            val signature = property.typeInfo.qualifiedName
            if (propertySignatures.containsKey(property.name)) {
                if (propertySignatures[property.name] != signature) {
                    conflictingProperties.add(property.name)
                    propertySignatures.remove(property.name)
                }
            } else {
                propertySignatures[property.name] = signature
            }
        }
    }

    val valueClassNames = versions.flatMap { version ->
        version.properties.filterIsInstance<Property.Property>().map { property ->
            val name = if (conflictingProperties.contains(property.name)) {
                "$baseName${version.name}${property.name.replaceFirstChar { it.uppercaseChar() }}"
            } else {
                "$baseName${property.name.replaceFirstChar { it.uppercaseChar() }}"
            }
            (version.name to property.name) to name
        }
    }.toMap()

    val topLevelClass = TypeSpec.classBuilder(generatedClassName)
        .addModifiers(KModifier.SEALED)
        .addAnnotation(ClassName("kotlinx.serialization", "Serializable"))
        .addKdoc(TOP_LEVEL_CLASS_KDOC, baseName)

    sortedVersions.forEach { version ->
        val versionClass = TypeSpec.classBuilder(version.name)
            .addModifiers(KModifier.SEALED)
            .superclass(ClassName(version.packageName, generatedClassName))
            .addAnnotation(ClassName("kotlinx.serialization", "Serializable"))
            .addKdoc(generateVersionBoxKdoc(version.name, version.schemaVersion!!))

        val dataTransferObjectSpecs = listOf(
            fieldsForBase(version) to Variant.BASE,
            fieldsForCreate(version) to Variant.CREATE,
            fieldsForPatch(version) to Variant.PATCH
        ).mapNotNull { (fields, variant) ->
            if (fields.isNotEmpty()) dataClassBuilder(version, fields, variant, valueClassNames) else null
        }
        dataTransferObjectSpecs.forEach { versionClass.addType(it) }
        topLevelClass.addType(versionClass.build())
    }
    fileBuilder.addType(topLevelClass.build())

    val allValueClassData = versions.flatMap { version ->
        version.properties.filterIsInstance<Property.Property>().map { property ->
            Triple(property, version, valueClassNames[version.name to property.name]!!)
        }
    }.distinctBy { it.third }

    val (conflictedData, sharedData) = allValueClassData.partition { (property, _, _) ->
        conflictingProperties.contains(property.name)
    }

    if (sharedData.isNotEmpty()) {
        val sharedValueClasses = sharedData.map { (property, version, className) ->
            val isSerializable =
                version.annotations?.any { it.qualifiedName == "kotlinx.serialization.Serializable" } == true
            buildValueClass(className, property, isSerializable)
        }.sortedBy { it.name }
        fileBuilder.addTypesWithHeader(sharedValueClasses, SHARED_VALUE_CLASSES_KDOC)
    }

    if (conflictedData.isNotEmpty()) {
        val conflictedValueClasses = conflictedData.map { (property, version, className) ->
            val isSerializable =
                version.annotations?.any { it.qualifiedName == "kotlinx.serialization.Serializable" } == true
            buildValueClass(className, property, isSerializable)
        }.sortedBy { it.name }
        fileBuilder.addTypesWithHeader(conflictedValueClasses, CONFLICTED_VALUE_CLASSES_KDOC)
    }

    fileBuilder.build().writeTo(outputDirectory)
}

private fun generateStandaloneModel(model: Model, outputDirectory: File) {
    val fileBuilder = FileSpec.builder(model.packageName, model.name + "Schema")
    fileBuilder
        .addFileComment(FILE_HEADER_COMMENT)
        .addOptInMarkersForModels(listOf(model))

    val valueClassNames = model.properties
        .filterIsInstance<Property.Property>()
        .associate { (model.name to it.name) to "${model.name}${it.name.replaceFirstChar { c -> c.uppercaseChar() }}" }

    val dataTransferObjectSpecs = listOf(
        fieldsForBase(model) to Variant.BASE,
        fieldsForCreate(model) to Variant.CREATE,
        fieldsForPatch(model) to Variant.PATCH
    ).mapNotNull { (fields, variant) ->
        if (fields.isNotEmpty()) dataClassBuilder(model, fields, variant, valueClassNames) else null
    }
    dataTransferObjectSpecs.forEach { fileBuilder.addType(it) }

    val valueClassSpecs = model.properties
        .filterIsInstance<Property.Property>()
        .map { property ->
            val className = valueClassNames[model.name to property.name]!!
            val isSerializable =
                model.annotations?.any { it.qualifiedName == "kotlinx.serialization.Serializable" } == true
            buildValueClass(className, property, isSerializable)
        }
        .sortedBy { it.name }

    fileBuilder.addTypesWithHeader(valueClassSpecs, STANDALONE_VALUE_CLASSES_KDOC)
    fileBuilder.build().writeTo(outputDirectory)
}

private fun FileSpec.Builder.addTypesWithHeader(specs: List<TypeSpec>, header: String) {
    if (specs.isEmpty()) return
    val firstSpecWithHeader = specs.first().toBuilder().addKdoc(header).build()
    addType(firstSpecWithHeader)
    specs.drop(1).forEach { addType(it) }
}

private fun FileSpec.Builder.addOptInMarkersForModels(models: List<Model>): FileSpec.Builder = apply {
    models.flatMap { it.optInMarkers ?: emptyList() }
        .distinct()
        .forEach { fullyQualifiedName ->
            val packageName = fullyQualifiedName.substringBeforeLast('.')
            val simpleName = fullyQualifiedName.substringAfterLast('.')
            val markerClass = ClassName(packageName, simpleName)
            addAnnotation(
                AnnotationSpec.builder(ClassName("kotlin", "OptIn"))
                    .addMember("%T::class", markerClass)
                    .build()
            )
        }
}