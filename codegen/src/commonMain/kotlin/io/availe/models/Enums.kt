package io.availe.models

enum class Module { SHARED, SERVER }

enum class Replication { NONE, PATCH, CREATE, BOTH }

enum class Variant(val suffix: String) {
    BASE("Data"),
    CREATE("CreateRequest"),
    PATCH("PatchRequest")
}