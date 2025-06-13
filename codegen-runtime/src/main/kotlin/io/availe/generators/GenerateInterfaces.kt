package io.availe.generators

import com.squareup.kotlinpoet.FileSpec
import io.availe.builders.buildGenericInterface
import io.availe.models.Model
import io.availe.utils.packageName

fun generateInterfaces(models: List<Model>) {
    models.forEach { model ->
        val outputDir = filePath
        val interfaceFile = FileSpec.builder(packageName, "I${model.name}")
            .addType(buildGenericInterface(model))
            .build()
        interfaceFile.writeTo(outputDir)
    }
}