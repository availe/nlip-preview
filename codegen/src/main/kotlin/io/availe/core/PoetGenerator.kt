package io.availe.core

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
                replication = Replication.NONE
            )
        ),
        replication = Replication.NONE
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

    models.forEach {
        val baseTypes: List<Property> = fieldsForBase(it)
        val createTypes: List<Property> = fieldsForCreate(it)
        val patchTypes: List<Property> = fieldsForPatch(it)

        println("Generating for ${it.name}")
        println("Base:")
        baseTypes.forEach { prop -> println(" - ${prop.name}") }
        println("Create:")
        createTypes.forEach { prop -> println(" - ${prop.name}") }
        println("Patch:")
        patchTypes.forEach { prop -> println(" - ${prop.name}") }
        println("---")
    }

//    models.forEach { model ->
//        val baseType = generateDataClass(model, fieldsForBase(model), Variant.BASE)
//        FileSpec.builder(packageName, model.name)
//            .addType(baseType)
//            .build()
//            .writeTo(System.out)
//
//        val createProps = fieldsForCreate(model)
//        if (createProps.isNotEmpty()) {
//            val createType = generateDataClass(model, createProps, Variant.CREATE)
//            FileSpec.builder(packageName, model.name + Variant.CREATE.suffix)
//                .addType(createType)
//                .build()
//                .writeTo(System.out)
//        }
//
//        val patchProps = fieldsForPatch(model)
//        if (patchProps.isNotEmpty()) {
//            val patchType = generateDataClass(model, patchProps, Variant.PATCH)
//            FileSpec.builder(packageName, model.name + Variant.PATCH.suffix)
//                .addType(patchType)
//                .build()
//                .writeTo(System.out)
//        }
//    }
}
