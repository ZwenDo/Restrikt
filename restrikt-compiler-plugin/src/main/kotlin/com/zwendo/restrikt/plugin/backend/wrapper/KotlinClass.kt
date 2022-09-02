package com.zwendo.restrikt.plugin.backend.wrapper

import com.zwendo.restrikt.plugin.frontend.PluginConfiguration
import kotlinx.metadata.Flag
import kotlinx.metadata.Flags
import kotlinx.metadata.KmClass
import kotlinx.metadata.KmDeclarationContainer
import kotlinx.metadata.KmProperty
import kotlinx.metadata.KmPropertyExtensionVisitor
import kotlinx.metadata.impl.extensions.KmExtension
import kotlinx.metadata.jvm.JvmFieldSignature
import kotlinx.metadata.jvm.JvmMethodSignature
import kotlinx.metadata.jvm.JvmPropertyExtensionVisitor
import kotlinx.metadata.jvm.KotlinClassMetadata
import kotlinx.metadata.jvm.signature

internal class KotlinClass {

    private var forceSynthetic = false

    private val functions = hashMapOf<String, KotlinFunction>()

    private val properties = hashMapOf<String, KotlinProperty>()

    /**
     * Set of all synthetic elements of the class (function signatures and field names).
     */
    private val syntheticSymbols = hashSetOf<String>()

    /**
     * Gets a function by its signature.
     */
    fun function(name: String, descriptor: String): KotlinFunction {
        val signature = "$name$descriptor"
        // compute used for default constructor
        return functions.computeIfAbsent(signature) { KotlinFunction.new(signature) }
    }

    /**
     * Gets a property by its name.
     */
    fun property(descriptor: String) = properties[descriptor]

    /**
     * Makes a symbol synthetic.
     */
    fun makeSynthetic(symbol: String) {
        syntheticSymbols += symbol
    }

    fun isForceSynthetic(identifier: String): Boolean = identifier in syntheticSymbols

    fun setData(data: KotlinClassMetadata) {
        when (data) {
            is KotlinClassMetadata.Class -> {
                val clazz = data.toKmClass()

                isInternalValue = Flag.Common.IS_INTERNAL(clazz.flags)

                functions += functionMapFactory(clazz)
                functions += constructorsMapFactory(clazz)
                val (props, extraFunctions) = propertyMapFactory(clazz, this)
                properties += props
                functions += extraFunctions
            }
            is KotlinClassMetadata.FileFacade -> {
                val pkg = data.toKmPackage()
                functions += functionMapFactory(pkg)
                val (props, extraFunctions) = propertyMapFactory(pkg, this)
                properties += props
                functions += extraFunctions
            }
            is KotlinClassMetadata.MultiFileClassPart -> {
                val pkg = data.toKmPackage()
                functions += functionMapFactory(pkg)
                val (props, extraFunctions) = propertyMapFactory(pkg, this)
                properties += props
                functions += extraFunctions
            }
            else -> Unit
        }
    }

    private var isInternalValue = false

    val isInternal
        get() = isInternalValue

    val isForceSynthetic
        get() = forceSynthetic

    fun forceSynthetic() {
        forceSynthetic = true
    }

}

/**
 * Creates the map of functions of a kotlin class, either basic or toplevel.
 */
private fun functionMapFactory(container: KmDeclarationContainer) = container.functions.associateBy(
    { "${if ('<' == it.name[0]) it.name else ""}${it.signature!!.asString()}" }
) {
    KotlinFunction.new(it)
}.toMutableMap()

/**
 * Creates the map of constructors of a kotlin class (not toplevel).
 */
private fun constructorsMapFactory(inner: KmClass) = inner.constructors.associateBy(
    { "<init>${it.signature!!.asString()}" }
) {
    KotlinFunction.new(it)
}

/**
 * Creates the map of properties of a kotlin class. In addition gets the functions of the properties if they exist.
 */
private fun propertyMapFactory(
    container: KmDeclarationContainer,
    clazz: KotlinClass,
): Pair<MutableMap<String, KotlinProperty>, MutableMap<String, KotlinFunction>> {
    val extraFunctions = hashMapOf<String, KotlinFunction>()

    val properties = container.properties.associateBy(KmProperty::name) { property ->
        val wrapper = KotlinProperty(property)

        // if not internal and not force synthetic, we don't care about the rest
        if (!wrapper.isInternal && !clazz.isForceSynthetic(property.name)) return@associateBy wrapper

        // gets the property methods signatures
        val propertyExtension = property.visitExtensions(JvmPropertyExtensionVisitor.TYPE)
        val visitor = PropertyVisitor()

        // visit the property extensions
        @Suppress("UNCHECKED_CAST")
        (propertyExtension as KmExtension<KmPropertyExtensionVisitor>).accept(visitor)
        visitor.getterSignature?.let { extraFunctions[it] = SyntheticMethod }
        visitor.setterSignature?.let { extraFunctions[it] = SyntheticMethod }

        wrapper // add the property to the map
    }

    return properties.toMutableMap() to extraFunctions
}

/**
 * Singleton representing a simple internal method. Used to represents internal or force java hidden property accessors.
 */
private object SyntheticMethod : KotlinFunction {

    override fun isSynthetic(originClass: KotlinClass) = PluginConfiguration.automaticInternalHiding

}

/**
 * Class that visits a property to retrieve the getter and setter signatures
 */
private class PropertyVisitor : JvmPropertyExtensionVisitor(null) {

    var getterSignature: String? = null

    var setterSignature: String? = null

    override fun visit(
        jvmFlags: Flags,
        fieldSignature: JvmFieldSignature?,
        getterSignature: JvmMethodSignature?,
        setterSignature: JvmMethodSignature?,
    ) {
        this.getterSignature = getterSignature?.asString()
        this.setterSignature = setterSignature?.asString()
    }

}
