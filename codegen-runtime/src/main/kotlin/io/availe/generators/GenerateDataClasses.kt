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

    val valueClassNamesByBase = modelsByBaseName.mapValues { (base, versions) ->
        determineValueClassNames(base, versions)
    }

    val globalValueClasses: Set<String> = valueClassNamesByBase.values.flatMap { it.values }.toSet()

    modelsByBaseName.forEach { (baseName, versions) ->
        val mapForBase = valueClassNamesByBase[baseName]!!
        generateSchemaFile(baseName, versions, mapForBase, globalValueClasses)
    }
}

private fun generateSchemaFile(
    baseName: String,
    versions: List<Model>,
    valueClassNames: Map<Pair<String, String>, String>,
    existingValueClasses: Set<String>
) {
    val isVersioned = versions.first().isVersionOf != null
    val representativeModel = versions.first()
    val schemaFileName = (if (isVersioned) baseName else representativeModel.name) + SCHEMA_SUFFIX

    val fileBuilder = FileSpec.builder(representativeModel.packageName, schemaFileName)
        .addFileComment(FILE_HEADER_COMMENT)
        .addOptInMarkersForModels(versions)

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
    } else null

    versions.forEach { version ->
        val dtos = generateDataTransferObjects(version, valueClassNames, existingValueClasses)
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
            dtos.forEach { fileBuilder.addType(it) }
        }
    }

    topLevelClassBuilder?.let { fileBuilder.addType(it.build()) }

    generateAndAddValueClasses(
        fileBuilder = fileBuilder,
        baseName = baseName,
        versions = versions,
        valueClassNames = valueClassNames,
        existingValueClasses = existingValueClasses
    )

    fileBuilder.build().writeTo(OUTPUT_DIRECTORY)
}

private fun generateDataTransferObjects(
    model: Model,
    valueClassNames: Map<Pair<String, String>, String>,
    existingValueClasses: Set<String>
): List<TypeSpec> =
    listOf(
        fieldsForBase(model) to Variant.BASE,
        fieldsForCreate(model) to Variant.CREATE,
        fieldsForPatch(model) to Variant.PATCH
    ).mapNotNull { (fields, variant) ->
        if (fields.isNotEmpty()) {
            buildDataTransferObjectClass(
                model = model,
                properties = fields,
                variant = variant,
                valueClassNames = valueClassNames,
                existingValueClasses = existingValueClasses
            )
        } else null
    }

/* ---------- helpers ---------- */

private fun determineValueClassNames(
    baseName: String,
    versions: List<Model>
): Map<Pair<String, String>, String> {
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
    valueClassNames: Map<Pair<String, String>, String>,
    existingValueClasses: Set<String>
) {
    val allValueClassData =
        versions.flatMap { version ->
            version.properties
                .filterIsInstance<Property.Property>()
                .filterNot { prop ->
                    val skip = prop.typeInfo.isEnum ||
                            prop.typeInfo.isValueClass ||
                            existingValueClasses.contains(prop.typeInfo.qualifiedName) ||
                            prop.typeInfo.qualifiedName.startsWith("kotlin.collections.") ||
                            prop is Property.ForeignProperty
                    println(
                        "evaluate property=${prop.name} qualified=${prop.typeInfo.qualifiedName} " +
                                "isValueClass=${prop.typeInfo.isValueClass} " +
                                "globalSkip=${existingValueClasses.contains(prop.typeInfo.qualifiedName)} skip=$skip"
                    )
                    skip
                }
                .mapNotNull { property ->
                    valueClassNames[version.name to property.name]?.let { className ->
                        Triple(property, version, className)
                    }
                }
        }.distinctBy { it.third }

    println("allValueClassData size=${allValueClassData.size}")

    if (allValueClassData.isEmpty()) return

    val isStandalone = versions.size == 1 && versions.first().isVersionOf == null
    if (isStandalone) {
        val valueClassSpecs = allValueClassData.map { (property, version, className) ->
            val isSerializable =
                version.annotations?.any { it.qualifiedName == SERIALIZABLE_QUALIFIED_NAME } == true
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
            val sharedSpecs = sharedData.map { (property, version, className) ->
                val isSerializable =
                    version.annotations?.any { it.qualifiedName == SERIALIZABLE_QUALIFIED_NAME } == true
                buildValueClass(className, property, isSerializable)
            }.sortedBy { it.name }
            fileBuilder.addTypesWithHeader(sharedSpecs, SHARED_VALUE_CLASSES_KDOC)
        }

        if (conflictedData.isNotEmpty()) {
            val conflictedSpecs = conflictedData.map { (property, version, className) ->
                val isSerializable =
                    version.annotations?.any { it.qualifiedName == SERIALIZABLE_QUALIFIED_NAME } == true
                buildValueClass(className, property, isSerializable)
            }.sortedBy { it.name }
            fileBuilder.addTypesWithHeader(conflictedSpecs, CONFLICTED_VALUE_CLASSES_KDOC)
        }
    }
}

private fun FileSpec.Builder.addTypesWithHeader(
    specs: List<TypeSpec>,
    header: String
) {
    if (specs.isEmpty()) return
    val firstSpecBuilder = specs.first().toBuilder()
    firstSpecBuilder.kdoc.clear()
    firstSpecBuilder.addKdoc(header)
    addType(firstSpecBuilder.build())
    specs.drop(1).forEach { addType(it) }
}

private fun FileSpec.Builder.addOptInMarkersForModels(
    models: List<Model>
): FileSpec.Builder {
    val distinctMarkers = models.flatMap { it.optInMarkers ?: emptyList() }.distinct()
    if (distinctMarkers.isNotEmpty()) {
        val format = distinctMarkers.joinToString(", ") { "%T::class" }
        val args = distinctMarkers.map { fq ->
            val pkg = fq.substringBeforeLast('.')
            val cls = fq.substringAfterLast('.')
            ClassName(pkg, cls)
        }.toTypedArray()
        addAnnotation(
            AnnotationSpec.builder(ClassName("kotlin", "OptIn"))
                .addMember(format, *args)
                .build()
        )
    }
    return this
}
