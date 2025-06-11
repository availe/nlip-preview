package io.availe

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asTypeName
import io.availe.models.Model
import io.availe.models.Property

fun buildDefinitionsFile(models: List<Model>): FileSpec {
    val funSpec = FunSpec.builder("definitions")
        .returns(List::class.asTypeName().parameterizedBy(Model::class.asTypeName()))
        .addCode(buildModelsList(models))
        .build()

    return FileSpec.builder("io.availe.definitions", "GeneratedModels")
        .addFunction(funSpec)
        .build()
}

private fun buildModelsList(models: List<Model>): String {
    val modelsListCode = models.joinToString(",\n") { model ->
        val propertiesListCode = model.properties.joinToString(",\n") { prop ->
            when (prop) {
                is Property.Property -> """
                    Property.Property(
                        name = "${prop.name}",
                        underlyingType = "${prop.underlyingType}",
                        optional = ${prop.optional},
                        replication = io.availe.models.Replication.${prop.replication.name}
                    )
                """.trimIndent()
                is Property.ForeignProperty -> """
                    Property.ForeignProperty(
                        name = "${prop.name}",
                        foreignModelName = "${prop.foreignModelName}",
                        property = Property.Property(name="id", underlyingType="kotlin.Long", optional=false, replication=io.availe.models.Replication.BOTH),
                        optional = ${prop.optional},
                        replication = io.availe.models.Replication.${prop.replication.name}
                    )
                """.trimIndent()
            }
        }
        """
            Model(
                name = "${model.name}",
                module = io.availe.models.Module.${model.module.name},
                properties = listOf(
                    ${propertiesListCode.prependIndent("            ")}
                ),
                replication = io.availe.models.Replication.${model.replication.name}
            )
        """.trimIndent()
    }
    return "return listOf(\n${modelsListCode.prependIndent("    ")}\n)"
}