package com.zwendo.restrikt.plugin.backend.symbol

import com.zwendo.restrikt.annotation.HideFromJava
import com.zwendo.restrikt.annotation.PackagePrivate
import com.zwendo.restrikt.plugin.backend.PACKAGE_PRIVATE_MASK
import com.zwendo.restrikt.plugin.frontend.PluginConfiguration
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.org.objectweb.asm.AnnotationVisitor
import org.jetbrains.org.objectweb.asm.Opcodes

/**
 * Supertype for complex symbols, class-like symbols, methods, fields and record components.
 */
internal sealed class AbstractSymbolData<E>(
    private val classData: ClassData?,
    private val baseAccess: Int,
    private val symbolFactory: (Int) -> E,
) : WritableSymbolData<E> {

    private val actions = mutableListOf<E.() -> Unit>()

    /*
      'field' checks are only for symbols visited before the writing phase (e.g. methods or fields).
      Classes are most of the time visited after the writing phase, so we can't rely on the 'field' check.
      Empty interfaces/annotation classes are visited before the writing phase.

             Methods, Fields, Classes (rarely)             Classes most common case
         ------------------------------------------ ------------------------------------------
         symbol encountered                        | symbol encountered
         (writing delayed)                         | (writing delayed)
                 |                                 |         |
                 v                                 |         v
          visitor visits  -> "field"s are updated  |  contained symbols -> retrieves the class' descriptor and set it
            the symbol                             |   are encountered
                 |                                 |         |
                 v                                 |         v
            writing phase -> "field"s are checked  |   writing phase    -> descriptor is used
    */

    private var isHiddenFromJava: Boolean = false
        get() = PluginConfiguration.hideFromJava.enabled && (field || hasAnnotation(HIDE_FROM_JAVA_FQNAME))

    private var isPackagePrivate: Boolean = false
        get() = PluginConfiguration.packagePrivate.enabled && (field || hasAnnotation(PACKAGE_PRIVATE_FQNAME))

    // 'field' here is only for internal interfaces/annotation classes
    protected var isInternal: Boolean = false
        get() = PluginConfiguration.automaticInternalHiding && (field || doesDescriptor { isInternal })

    private val isSynthetic: Boolean
        get() = isHiddenFromJava || isInternal || (classData as? AbstractSymbolData<*>)?.isSynthetic == true

    final override var isWritten: Boolean = false
        private set

    final override val canQueue: Boolean
        get() = !isWritten

    final override fun setPackagePrivate() {
        isPackagePrivate = true
    }

    final override fun hideFromJava() {
        isHiddenFromJava = true
    }

    override fun hideFromKotlin() = queueAction {
        generateDeprecatedHidden { d, s -> visitExtraAnnotation(d, s) }
    }

    final override fun queueAction(action: E.() -> Unit) {
        check(!isWritten) { "Cannot queue action after symbol has been written" }
        actions += action
    }

    override fun writeToClassFile(forceWriting: Boolean) {
        check(!isWritten) { "Cannot write symbol twice" }
        isWritten = true
        val symbol = createSymbol()
        symbolWriting(symbol)
    }

    protected open fun symbolWriting(symbol: E) = actions.forEach { symbol.it() }

    protected fun computeAccess(): Int {
        var access = baseAccess

        if (isSynthetic) access = access or Opcodes.ACC_SYNTHETIC
        if (isPackagePrivate) access = access and PACKAGE_PRIVATE_MASK

        return access
    }

    abstract val descriptor: DeclarationDescriptor?

    protected abstract fun doesDescriptor(check: DeclarationDescriptor.() -> Boolean): Boolean

    protected abstract fun E.visitExtraAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor


    private fun hasAnnotation(fqName: FqName): Boolean = doesDescriptor { annotations.hasAnnotation(fqName) }

    private fun createSymbol(): E = symbolFactory(computeAccess())

    private companion object {

        val HIDE_FROM_JAVA_FQNAME = FqName(HideFromJava::class.java.name)

        val PACKAGE_PRIVATE_FQNAME = FqName(PackagePrivate::class.java.name)

    }

}
