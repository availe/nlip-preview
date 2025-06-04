package io.availe.jooq

import org.jooq.codegen.DefaultGeneratorStrategy
import org.jooq.codegen.GeneratorStrategy.Mode
import org.jooq.meta.Definition
import org.jooq.meta.ForeignKeyDefinition
import org.jooq.meta.InverseForeignKeyDefinition
import org.jooq.tools.StringUtils

class CustomGeneratorStrategy : DefaultGeneratorStrategy() {

    init {
        println(">> CustomGeneratorStrategy has been instantiated")
    }

    override fun getJavaMemberName(definition: Definition, mode: Mode): String {
        return customName(definition)
            ?: super.getJavaMemberName(definition, mode)
    }

    override fun getJavaMethodName(definition: Definition, mode: Mode): String {
        return customName(definition)
            ?: super.getJavaMethodName(definition, mode)
    }

    private fun customName(definition: Definition): String? {
        return when (definition) {
            is ForeignKeyDefinition -> {
                if (definition.table.outputName == definition.referencedTable.outputName) {
                    println(">> [CustomStrategy] Self-FK detected (FK side): ${definition.name}")
                    val base = StringUtils.toCamelCase(definition.table.outputName)
                    base.replaceFirstChar { it.lowercaseChar() } + "Parent"
                } else null
            }

            is InverseForeignKeyDefinition -> {
                val fk = definition.foreignKey
                if (fk.table.outputName == fk.referencedTable.outputName) {
                    println(">> [CustomStrategy] Self-FK detected (Inverse side): ${fk.name}")
                    val base = StringUtils.toCamelCase(fk.table.outputName)
                    base.replaceFirstChar { it.lowercaseChar() } + "Children"
                } else null
            }

            else -> null
        }
    }
}
