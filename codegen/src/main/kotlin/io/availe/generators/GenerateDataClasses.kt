package io.availe.generators

import com.squareup.kotlinpoet.FileSpec
import io.availe.core.*
import io.availe.utils.outputDirForModule

fun generateDataClasses(models: List<Model>) {
    models.forEach { model ->
        val outputDir = outputDirForModule(model.module)

        val fileBuilder = FileSpec.builder(packageName, model.name)

        listOf(
            fieldsForBase(model) to Variant.BASE,
            fieldsForCreate(model) to Variant.CREATE,
            fieldsForPatch(model) to Variant.PATCH,
        ).forEach { (fields, variant) ->
            if (fields.isNotEmpty()) {
                fileBuilder.addType(generateDataClass(model, fields, variant))
            }
        }

        fileBuilder.build().writeTo(outputDir)
    }
}
