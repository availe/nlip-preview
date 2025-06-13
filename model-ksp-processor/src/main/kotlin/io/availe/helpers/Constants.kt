package io.availe.helpers

import io.availe.FieldGen
import io.availe.Hide
import io.availe.ModelGen
import io.availe.SchemaVersion

val MODEL_ANNOTATION_NAME: String = ModelGen::class.qualifiedName!!
val FIELD_ANNOTATION_NAME: String = FieldGen::class.qualifiedName!!
val SCHEMA_VERSION_ANNOTATION_NAME: String = SchemaVersion::class.qualifiedName!!
val HIDE_ANNOTATION_NAME: String = Hide::class.qualifiedName!!

const val REPLICATION_ARG: String = "replication"
const val ANNOTATIONS_ARG: String = "annotations"
const val OPT_IN_MARKERS_ARG: String = "optInMarkers"
const val SCHEMA_VERSION_ARG: String = "number"
const val ID_PROPERTY: String = "id"
const val SCHEMA_VERSION_FIELD: String = "schemaVersion"
