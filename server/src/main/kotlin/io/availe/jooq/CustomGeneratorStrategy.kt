package io.availe.jooq

import org.jooq.codegen.DefaultGeneratorStrategy
import org.jooq.meta.Definition
import org.jooq.meta.ForeignKeyDefinition
import org.jooq.meta.InverseForeignKeyDefinition

class CustomGeneratorStrategy : DefaultGeneratorStrategy() {
    override fun getJavaIdentifier(definition: Definition): String {
        if (definition is InverseForeignKeyDefinition) {
            val fk = definition.foreignKey as ForeignKeyDefinition
            if (fk.referencedTable.name == fk.table.name) {
                return fk.table.name
                    .removeSuffix("s")
                    .replaceFirstChar { it.lowercase() } + "Children"
            }
        }
        if (definition is ForeignKeyDefinition) {
            if (definition.referencedTable.name == definition.table.name) {
                return definition.table.name
                    .removeSuffix("s")
                    .replaceFirstChar { it.lowercase() } + "Parent"
            }
        }
        return super.getJavaIdentifier(definition)
    }
}
