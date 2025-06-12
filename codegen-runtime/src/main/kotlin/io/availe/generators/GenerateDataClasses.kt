package io.availe.generators

import com.squareup.kotlinpoet.FileSpec
import io.availe.builders.buildGenericInterface
import io.availe.builders.buildValueClass
import io.availe.builders.dataClassBuilder
import io.availe.builders.packageName
import io.availe.models.Model
import io.availe.models.Property
import io.availe.models.Variant
import io.availe.utils.fieldsForBase
import io.availe.utils.fieldsForCreate
import io.availe.utils.fieldsForPatch
import java.io.File

val filePath: File = File("build/generated-src/kotlin-poet")

fun generateDataClasses(models: List<Model>) {
    val out: File = filePath
    models.forEach { m ->
        val file = FileSpec.builder(packageName, m.name)
        listOf(
            fieldsForBase(m) to Variant.BASE,
            fieldsForCreate(m) to Variant.CREATE,
            fieldsForPatch(m) to Variant.PATCH
        ).forEach { (f, v) -> if (f.isNotEmpty()) file.addType(dataClassBuilder(m, f, v)) }
        file.addType(buildGenericInterface(m))
        m.properties.filterIsInstance<Property.Property>().forEach { file.addType(buildValueClass(m, it)) }
        file.build().writeTo(out)
    }
}
