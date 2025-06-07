package io.availe.core

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import kotlin.reflect.KClass

enum class Module { SHARED, SERVER }

data class InlineWrapper(val name: String, val backing: KClass<*>)
data class EnumSpec(val name: String, val values: List<String>, val nestedIn: String? = null)
data class ModelSpec(val name: String, val props: List<PropertySpecData>, val module: Module)
data class PropertySpecData(val name: String, val type: TypeName)

class CodegenBuilder {
    internal val wrappers = mutableListOf<InlineWrapper>()
    internal val enums = mutableListOf<EnumSpec>()
    internal val models = mutableListOf<ModelSpec>()
    fun valueClass(name: String, backing: KClass<*>) {
        wrappers += InlineWrapper(name, backing)
    }

    fun enum(name: String, values: List<String>, nestedIn: String? = null) {
        enums += EnumSpec(name, values, nestedIn)
    }

    fun model(name: String, module: Module = Module.SHARED, block: ModelBuilder.() -> Unit) {
        models += ModelBuilder(name, module).apply(block).build()
    }
}

class ModelBuilder(private val modelName: String, private val module: Module) {
    private val _props = mutableListOf<PropertySpecData>()
    fun prop(name: String, typeName: String) {
        _props += PropertySpecData(name, ClassName("io.availe.models", typeName))
    }

    fun prop(name: String, klass: KClass<*>) {
        _props += PropertySpecData(name, klass.asTypeName())
    }

    internal fun build() = ModelSpec(modelName, _props.toList(), module)
}

fun codegen(block: CodegenBuilder.() -> Unit) = CodegenBuilder().apply(block)
