package com.zwendo.restrikt.plugin.backend.wrapper

import kotlinx.metadata.Flag
import kotlinx.metadata.Flags
import kotlinx.metadata.KmClass
import kotlinx.metadata.KmDeclarationContainer
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
    fun function(signature: String): KotlinFunction = functions.computeIfAbsent(signature, ::KotlinFunction)

    /**
     * Gets a property by its name.
     */
    fun property(descriptor: String) = properties.computeIfAbsent(descriptor, ::KotlinProperty)

    /**
     * Makes a symbol synthetic.
     */
    fun makeSynthetic(symbol: String) {
        syntheticSymbols += symbol
    }

    fun isForceSynthetic(identifier: String): Boolean = identifier in syntheticSymbols

    fun setData(data: KotlinClassMetadata) = when (data) {
        is KotlinClassMetadata.Class -> {
            val clazz = data.toKmClass()

            isInternalValue = Flag.Common.IS_INTERNAL(clazz.flags)
            fillWithFunctions(clazz)
            fillWithConstructors(clazz)
            fillWithProperties(clazz)
        }
        is KotlinClassMetadata.FileFacade -> {
            val pkg = data.toKmPackage()

            fillWithFunctions(pkg)
            fillWithProperties(pkg)
        }
        is KotlinClassMetadata.MultiFileClassPart -> {
            val pkg = data.toKmPackage()

            fillWithFunctions(pkg)
            fillWithProperties(pkg)
        }
        else -> Unit
    }

    private var isInternalValue = false

    val isInternal
        get() = isInternalValue

    val isForceSynthetic
        get() = forceSynthetic

    fun forceSynthetic() {
        forceSynthetic = true
    }

    /**
     * Fills the functions with their kotlin data.
     */
    private fun fillWithFunctions(container: KmDeclarationContainer) = container.functions.forEach {
        val jvmSignature = it.signature ?: return@forEach
        val signature = "${if ('<' == it.name[0]) it.name else ""}${jvmSignature.asString()}"
        functions[signature]?.setData(it)
    }

    /**
     * Fills constructors with their kotlin data.
     */
    private fun fillWithConstructors(inner: KmClass) = inner.constructors.forEach {
        val jvmSignature = it.signature ?: return@forEach
        val signature = "<init>${jvmSignature.asString()}"
        functions[signature]?.setData(it)
    }

    /**
     * Fills the properties with their kotlin data and potentiality creates extra functions.
     */
    private fun fillWithProperties(container: KmDeclarationContainer) {
        container.properties.forEach { property ->
            val wrapper = properties[property.name] ?: return@forEach
            wrapper.setData(property)

            // if not internal and not force synthetic, we don't care about the rest
            if (!wrapper.isInternal && !isForceSynthetic(property.name)) return@forEach

            // gets the property methods signatures
            val propertyExtension = property.visitExtensions(JvmPropertyExtensionVisitor.TYPE)
            val visitor = PropertyVisitor()

            // visit the property extensions
            @Suppress("UNCHECKED_CAST")
            (propertyExtension as KmExtension<KmPropertyExtensionVisitor>).accept(visitor)
            visitor.getterSignature?.let { functions[it]?.setInternal() }
            visitor.setterSignature?.let { functions[it]?.setInternal() }
        }
    }

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
