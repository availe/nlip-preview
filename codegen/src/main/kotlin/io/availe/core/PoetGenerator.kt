package io.availe.core

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.typeNameOf
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
fun main() {
    val messageModel = Model(
        name = "Message",
        module = Module.SHARED,
        properties = listOf(
            Property.Property(
                name = "id",
                underlyingType = typeNameOf<String>(),
                optional = false,
                replication = Replication.BOTH
            )
        ),
        replication = Replication.BOTH
    )

    val internalMessageModel = Model(
        name = "InternalMessage",
        module = Module.SHARED,
        properties = listOf(
            Property.Property(
                name = "meta",
                underlyingType = typeNameOf<String>(),
                optional = false,
                replication = Replication.BOTH
            ),
            Property.ForeignProperty(
                name = "message",
                property = Property.Property(
                    name = "id",
                    underlyingType = typeNameOf<String>(),
                    optional = false,
                    replication = Replication.BOTH
                ),
                optional = false,
                replication = Replication.BOTH
            )
        ),
        replication = Replication.BOTH
    )

    val models = listOf(messageModel, internalMessageModel)

    models.forEach { model ->
        val typeSpec = generateDataClass(model, fieldsForBase(model))
        val fileSpec = FileSpec.builder(packageName, model.name)
            .addType(typeSpec)
            .build()
        fileSpec.writeTo(System.out)
    }
}