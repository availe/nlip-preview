package io.availe

import java.io.File

internal val OUTPUT_DIRECTORY = File("build/generated-src/kotlin-poet")
internal const val MODELS_PACKAGE_NAME = "io.availe.models"

internal const val SCHEMA_SUFFIX = "Schema"
internal const val INTERFACE_PREFIX = "I"
internal const val TYPE_VARIABLE_PREFIX = "T_"

internal const val PATCHABLE_CLASS_NAME = "Patchable"
internal const val UNCHANGED_OBJECT_NAME = "Unchanged"
internal const val SET_CLASS_NAME = "Set"

internal const val SCHEMA_VERSION_PROPERTY_NAME = "schemaVersion"
internal const val VALUE_PROPERTY_NAME = "value"

internal const val OPT_IN_QUALIFIED_NAME = "kotlin.OptIn"
internal const val SERIALIZABLE_QUALIFIED_NAME = "kotlinx.serialization.Serializable"