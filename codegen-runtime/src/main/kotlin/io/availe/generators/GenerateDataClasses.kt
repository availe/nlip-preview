package io.availe.generators

import com.squareup.kotlinpoet.*
import io.availe.OUTPUT_DIRECTORY
import io.availe.SCHEMA_SUFFIX
import io.availe.SERIALIZABLE_QUALIFIED_NAME
import io.availe.builders.*
import io.availe.models.Model
import io.availe.models.Property
import io.availe.models.Variant
import io.availe.utils.fieldsForBase
import io.availe.utils.fieldsForCreate
import io.availe.utils.fieldsForPatch

fun generateDataClasses(models: List<Model>) {
    val modelsByBaseName = models.groupBy { it.isVersionOf ?: it.name }

    modelsByBaseName.forEach { (baseName, versions) ->
        generateSchemaFile(baseName, versions)
    }
}

private fun generateSchemaFile(baseName: String, versions: List<Model>) {
    val isVersioned = versions.first().isVersionOf != null
    val representativeModel = versions.first()
    val schemaFileName = (if (isVersioned) baseName else representativeModel.name) + SCHEMA_SUFFIX

    val fileBuilder = FileSpec.builder(representativeModel.packageName, schemaFileName)
        .addFileComment(FILE_HEADER_COMMENT)
        .addOptInMarkersForModels(versions)

    val valueClassNames = determineValueClassNames(baseName, versions)

    val topLevelClassBuilder = if (isVersioned) {
        TypeSpec.classBuilder(schemaFileName)
            .addModifiers(KModifier.SEALED)
            .addAnnotation(
                ClassName(
                    SERIALIZABLE_QUALIFIED_NAME.substringBeforeLast('.'),
                    SERIALIZABLE_QUALIFIED_NAME.substringAfterLast('.')
                )
            )
            .addKdoc(TOP_LEVEL_CLASS_KDOC, baseName)
    } else {
        null
    }

    versions.forEach { version ->
        val dtos = generateDataTransferObjects(version, valueClassNames)
        if (isVersioned) {
            val versionClass = TypeSpec.classBuilder(version.name)
                .addModifiers(KModifier.SEALED)
                .superclass(ClassName(version.packageName, schemaFileName))
                .addAnnotation(
                    ClassName(
                        SERIALIZABLE_QUALIFIED_NAME.substringBeforeLast('.'),
                        SERIALIZABLE_QUALIFIED_NAME.substringAfterLast('.')
                    )
                )
                .addKdoc(generateVersionBoxKdoc(version.name, version.schemaVersion!!))
                .addTypes(dtos)
                .build()
            topLevelClassBuilder?.addType(versionClass)
        } else {
            fileBuilder.addTypes(dtos)
        }
    }

    topLevelClassBuilder?.let { fileBuilder.addType(it.build()) }

    generateAndAddValueClasses(fileBuilder, baseName, versions, valueClassNames)

    fileBuilder.build().writeTo(OUTPUT_DIRECTORY)
}

private fun generateDataTransferObjects(
    model: Model,
    valueClassNames: Map<Pair<String, String>, String>
): List<TypeSpec> {
    return listOf(
        fieldsForBase(model) to Variant.BASE,
        fieldsForCreate(model) to Variant.CREATE,
        fieldsForPatch(model) to Variant.PATCH
    ).mapNotNull { (fields, variant) ->
        if (fields.isNotEmpty()) buildDataTransferObjectClass(model, fields, variant, valueClassNames) else null
    }
}

private fun determineValueClassNames(baseName: String, versions: List<Model>): Map<Pair<String, String>, String> {
    if (versions.size == 1 && versions.first().isVersionOf == null) {
        val model = versions.first()
        return model.properties
            .filterIsInstance<Property.Property>()
            .associate { (model.name to it.name) to "${model.name}${it.name.replaceFirstChar { c -> c.uppercaseChar() }}" }
    }

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

    return versions.flatMap { version ->
        version.properties.filterIsInstance<Property.Property>().map { property ->
            val valueClassName = if (conflictingProperties.contains(property.name)) {
                "$baseName${version.name}${property.name.replaceFirstChar { it.uppercaseChar() }}"
            } else {
                "$baseName${property.name.replaceFirstChar { it.uppercaseChar() }}"
            }
            (version.name to property.name) to valueClassName
        }
    }.toMap()
}

private fun generateAndAddValueClasses(
    fileBuilder: FileSpec.Builder,
    baseName: String,
    versions: List<Model>,
    valueClassNames: Map<Pair<String, String>, String>
) {
    val allValueClassData = versions.flatMap { version ->
        version.properties.filterIsInstance<Property.Property>().map { property ->
            Triple(property, version, valueClassNames[version.name to property.name]!!)
        }
    }.distinctBy { it.third }

    if (allValueClassData.isEmpty()) return

    val isStandalone = versions.size == 1 && versions.first().isVersionOf == null
    if (isStandalone) {
        val valueClassSpecs = allValueClassData.map { (property, version, className) ->
            val isSerializable = version.annotations?.any { it.qualifiedName == SERIALIZABLE_QUALIFIED_NAME } == true
            buildValueClass(className, property, isSerializable)
        }.sortedBy { it.name }
        fileBuilder.addTypesWithHeader(valueClassSpecs, STANDALONE_VALUE_CLASSES_KDOC)
    } else {
        val conflictingPropertyNames = valueClassNames.values
            .filter { it.startsWith(baseName + "V") }
            .map { it.removePrefix(baseName).drop(2).replaceFirstChar { c -> c.lowercaseChar() } }
            .toSet()

        val (conflictedData, sharedData) = allValueClassData.partition { (property, _, _) ->
            conflictingPropertyNames.contains(property.name)
        }

        if (sharedData.isNotEmpty()) {
            val sharedValueClasses = sharedData.map { (property, version, className) ->
                val isSerializable =
                    version.annotations?.any { it.qualifiedName == SERIALIZABLE_QUALIFIED_NAME } == true
                buildValueClass(className, property, isSerializable)
            }.sortedBy { it.name }
            fileBuilder.addTypesWithHeader(sharedValueClasses, SHARED_VALUE_CLASSES_KDOC)
        }

        if (conflictedData.isNotEmpty()) {
            val conflictedValueClasses = conflictedData.map { (property, version, className) ->
                val isSerializable =
                    version.annotations?.any { it.qualifiedName == SERIALIZABLE_QUALIFIED_NAME } == true
                buildValueClass(className, property, isSerializable)
            }.sortedBy { it.name }
            fileBuilder.addTypesWithHeader(conflictedValueClasses, CONFLICTED_VALUE_CLASSES_KDOC)
        }
    }
}

private fun FileSpec.Builder.addTypesWithHeader(specs: List<TypeSpec>, header: String) {
    if (specs.isEmpty()) return
    val firstSpecWithHeader = specs.first().toBuilder().addKdoc(header).build()
    addType(firstSpecWithHeader)
    specs.drop(1).forEach { addType(it) }
}

private fun FileSpec.Builder.addOptInMarkersForModels(models: List<Model>): FileSpec.Builder {
    val distinctMarkers = models.flatMap { it.optInMarkers ?: emptyList() }.distinct()

    if (distinctMarkers.isNotEmpty()) {
        val format = distinctMarkers.joinToString(", ") { "%T::class" }
        val arguments = distinctMarkers.map { fullyQualifiedName ->
            val packageName = fullyQualifiedName.substringBeforeLast('.')
            val simpleName = fullyQualifiedName.substringAfterLast('.')
            ClassName(packageName, simpleName)
        }.toTypedArray()

        addAnnotation(
            AnnotationSpec.builder(ClassName("kotlin", "OptIn"))
                .addMember(format, *arguments)
                .build()
        )
    }
    return this
}