package com.zwendo.restrikt.plugin.backend.wrapper

import com.zwendo.restrikt.plugin.backend.PACKAGE_PRIVATE_MASK
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
import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOriginKind
import org.jetbrains.org.objectweb.asm.Opcodes

internal class KotlinClass(val name: String) : KotlinSymbol {

    private var isForceSynthetic = false

    private val functions = hashMapOf<String, KotlinFunction>()

    private val properties = hashMapOf<String, KotlinProperty>()

    private var isPackagePrivate = false

    private var isInternal = false

    private var needsPrivateConstructor: Boolean = false
        get() {
            val result = field
            field = false
            return result
        }

    /**
     * Gets a function by its signature.
     */
    fun function(signature: String): KotlinFunction = functions.computeIfAbsent(signature) { KotlinFunction() }

    /**
     * Gets a property by its name.
     */
    fun property(descriptor: String): KotlinProperty = properties.computeIfAbsent(descriptor) { KotlinProperty() }

    fun onMemberDeclaration(builder: ClassBuilder) {
        if (needsPrivateConstructor) {
            generatePrivateConstructor(builder)
        }
    }

    fun setData(data: KotlinClassMetadata) = when (data) {
        is KotlinClassMetadata.Class -> {
            val clazz = data.toKmClass()

            isInternal = Flag.Common.IS_INTERNAL(clazz.flags)
            fillWithFunctions(clazz)
            fillWithConstructors(clazz)
            fillWithProperties(clazz)
        }
        is KotlinClassMetadata.FileFacade -> {
            val pkg = data.toKmPackage()

            needsPrivateConstructor = true
            fillWithFunctions(pkg)
            fillWithProperties(pkg)
        }
        is KotlinClassMetadata.MultiFileClassPart -> {
            val pkg = data.toKmPackage()

            fillWithFunctions(pkg)
            fillWithProperties(pkg)
        }
        is KotlinClassMetadata.MultiFileClassFacade -> {
            needsPrivateConstructor = true
        }
        else -> Unit
    }

    override fun forceSynthetic() {
        isForceSynthetic = true
    }

    override fun setPackagePrivate() {
        isPackagePrivate = true
    }

    fun computeModifiers(access: Int): Int {
        var actualAccess: Int = access

        if (isInternal || isForceSynthetic) {
            actualAccess = actualAccess or Opcodes.ACC_SYNTHETIC
        }

        if (isPackagePrivate) {
            actualAccess = actualAccess and PACKAGE_PRIVATE_MASK
        }

        return actualAccess
    }

    /**
     * Fills the functions with their kotlin data.
     */
    private fun fillWithFunctions(container: KmDeclarationContainer) = container.functions.forEach {
        val jvmSignature = it.signature ?: return@forEach
        functions[jvmSignature.asString()]?.setData(it)
    }

    /**
     * Fills constructors with their kotlin data.
     */
    private fun fillWithConstructors(inner: KmClass) = inner.constructors.forEach {
        val jvmSignature = it.signature ?: return@forEach
        functions[jvmSignature.asString()]?.setData(it)
    }

    /**
     * Fills the properties with their kotlin data and potentiality creates extra functions.
     */
    private fun fillWithProperties(container: KmDeclarationContainer) {
        container.properties.forEach { property ->
            val wrapper = properties[property.name] ?: return@forEach
            wrapper.setData(property)

            // gets the property extension
            val propertyExtension = property.visitExtensions(JvmPropertyExtensionVisitor.TYPE)
            val visitor = PropertyVisitor()

            // visit the property extensions
            @Suppress("UNCHECKED_CAST")
            (propertyExtension as KmExtension<KmPropertyExtensionVisitor>).accept(visitor)

            // now, we must apply property modifiers to its functions

            // retrieve annotation function to get annotations for the whole property
            val annotationFunction = visitor.annotationsFunctionSignature?.let { function(it) }
            visitor.getterSignature?.let { wrapper.applyModifiersToFunction(function(it), annotationFunction) }
            visitor.setterSignature?.let { wrapper.applyModifiersToFunction(function(it), annotationFunction) }

            annotationFunction?.removeAll() // remove all modifiers because function is not intended to be used
        }
    }

    private fun generatePrivateConstructor(builder: ClassBuilder) {
        val origin = JvmDeclarationOrigin(JvmDeclarationOriginKind.OTHER, null, null, null)
        val name = "<init>"
        val desc = "()V"
        val visitor = builder.newMethod(origin, Opcodes.ACC_PRIVATE, name, desc, null, null)
        visitor.apply {
            visitCode()
            visitVarInsn(Opcodes.ALOAD, 0)
            visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", name, desc, false)
            visitInsn(Opcodes.RETURN)
            visitMaxs(1, 1)
            visitEnd()
        }
    }

}

/**
 * Class that visits a property to retrieve the getter and setter signatures
 */
private class PropertyVisitor : JvmPropertyExtensionVisitor(null) {

    var getterSignature: String? = null

    var setterSignature: String? = null

    var annotationsFunctionSignature: String? = null

    override fun visit(
        jvmFlags: Flags,
        fieldSignature: JvmFieldSignature?,
        getterSignature: JvmMethodSignature?,
        setterSignature: JvmMethodSignature?,
    ) {
        this.getterSignature = getterSignature?.asString()
        this.setterSignature = setterSignature?.asString()
    }

    override fun visitSyntheticMethodForAnnotations(signature: JvmMethodSignature?) {
        annotationsFunctionSignature = signature?.asString()
    }
}
