package io.availe.generators

import com.squareup.kotlinpoet.FileSpec
import io.availe.builders.buildValueClass
import io.availe.builders.packageName
import io.availe.models.Model
import io.availe.models.Property
import io.availe.utils.outputDirForModule

fun generateValueClasses(models: List<Model>) {
    val grouped = models.groupBy { it.module }

    grouped.forEach { (module, model) ->
        val outputDir = outputDirForModule(module)

        val fileName = "Identifiers"

        val fileBuilder = FileSpec.builder(packageName, fileName)

        model.forEach { model ->
            model
                .properties
                .filterIsInstance<Property.Property>()
                .forEach { prop ->
                    val typeSpec = buildValueClass(model, prop)
                    fileBuilder.addType(typeSpec)
                }

        }

        fileBuilder.build().writeTo(outputDir)
    }

}