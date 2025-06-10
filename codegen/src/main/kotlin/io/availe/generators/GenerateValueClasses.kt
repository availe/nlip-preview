package io.availe.generators

import com.squareup.kotlinpoet.FileSpec
import io.availe.core.Model
import io.availe.core.Property
import io.availe.core.buildValueClass
import io.availe.core.packageName
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