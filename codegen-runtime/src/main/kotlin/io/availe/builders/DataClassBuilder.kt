package io.availe.builders

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.availe.models.Model
import io.availe.models.Property
import io.availe.models.Replication
import io.availe.models.Variant

const val packageName: String = "io.availe.models"
private const val BASE_IMPL_SUFFIX = "Data"

private fun String.asClassName(): ClassName {
    val clean = substringBefore('<').removeSuffix("?")
    val pkg = clean.substringBeforeLast('.')
    val type = clean.substringAfterLast('.')
    return ClassName(pkg, type)
}

private fun baseImplSuffixFor(variant: Variant): String =
    if (variant == Variant.BASE) BASE_IMPL_SUFFIX else variant.suffix

private fun coreType(model: Model, prop: Property, variant: Variant): TypeName {
    val suffix = baseImplSuffixFor(variant)
    val raw = when (prop) {
        is Property.Property -> ClassName(packageName, model.name + prop.name.replaceFirstChar { it.uppercaseChar() })
        is Property.ForeignProperty -> ClassName(packageName, prop.foreignModelName + suffix)
    }
    return raw
}

fun resolvedTypeName(model: Model, prop: Property, variant: Variant): TypeName {
    val inner = coreType(model, prop, variant)
    return if (variant == Variant.PATCH) ClassName(packageName, "Patchable").parameterizedBy(inner) else inner
}

fun dataClassBuilder(model: Model, props: List<Property>, variant: Variant): TypeSpec {
    val name = model.name + variant.suffix
    val typeSpec = TypeSpec.classBuilder(name).addModifiers(KModifier.DATA)
    model.annotations?.forEach {
        typeSpec.addAnnotation(it.asClassName())
    }
    val ctor = FunSpec.constructorBuilder()
    props.forEach { p ->
        val t = resolvedTypeName(model, p, variant)
        val param = ParameterSpec.builder(p.name, t)
        if (variant == Variant.PATCH) param.defaultValue("%T.Unchanged", ClassName(packageName, "Patchable"))
        ctor.addParameter(param.build())
    }
    typeSpec.primaryConstructor(ctor.build())
    val ifaceProps = model.properties.filter { it.replication == Replication.BOTH }
    val implement = (variant == Variant.BASE || variant == Variant.CREATE) && ifaceProps.isNotEmpty()
    props.forEach { p ->
        val t = resolvedTypeName(model, p, variant)
        val propSpec = PropertySpec.builder(p.name, t).initializer(p.name)
        p.annotations?.forEach {
            propSpec.addAnnotation(it.asClassName())
        }
        if (implement && ifaceProps.any { it.name == p.name }) propSpec.addModifiers(KModifier.OVERRIDE)
        typeSpec.addProperty(propSpec.build())
    }
    if (implement) {
        val iface = ClassName(packageName, "I${model.name}")
        val args = ifaceProps.map { resolvedTypeName(model, it, variant) }
        typeSpec.addSuperinterface(iface.parameterizedBy(args))
    }
    return typeSpec.build()
}