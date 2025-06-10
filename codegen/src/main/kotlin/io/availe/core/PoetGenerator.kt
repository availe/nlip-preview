package io.availe.core

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.typeNameOf
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
fun main() {
    val messageModelDefinition = Model(
        name = "Message",
        module = Module.SHARED,
        properties = listOf(
            Property.Property(
                name = "id",
                underlyingType = typeNameOf<String>(),
                optional = false,
                replication = Replication.PATCH
            )
        ),
        replication = Replication.PATCH
    )

    val internalMessageModelDefinition = Model(
        name = "InternalMessage",
        module = Module.SHARED,
        properties = listOf(
            Property.Property(
                name = "meta",
                underlyingType = typeNameOf<String>(),
                optional = true,
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

    val modelDefinitions = listOf(messageModelDefinition, internalMessageModelDefinition)

    modelDefinitions.forEach { model ->
        generateValueClasses(model).forEach { vcSpec ->
            FileSpec.builder(packageName, vcSpec.name!!)
                .addType(vcSpec)
                .build()
                .writeTo(System.out)
        }

        listOf(Variant.BASE, Variant.CREATE, Variant.PATCH).forEach { variant ->
            val props = when (variant) {
                Variant.BASE -> fieldsForBase(model)
                Variant.CREATE -> fieldsForCreate(model)
                Variant.PATCH -> fieldsForPatch(model)
            }
            val className = model.name + variant.suffix
            val typeSpec = generateDataClass(model, props, variant)

            FileSpec.builder(packageName, className)
                .addType(typeSpec)
                .build()
                .writeTo(System.out)
        }
    }
}
