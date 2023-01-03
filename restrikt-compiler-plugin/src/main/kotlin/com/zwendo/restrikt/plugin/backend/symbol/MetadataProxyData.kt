package com.zwendo.restrikt.plugin.backend.symbol

import org.jetbrains.org.objectweb.asm.AnnotationVisitor

internal class MetadataProxyData(
    private val classData: ClassData,
    private val factory: () -> AnnotationVisitor,
) : WritableSymbolData<AnnotationVisitor> {

    private val actions = mutableListOf<AnnotationVisitor.() -> Unit>()

    override fun writeToClassFile(forceWriting: Boolean) {
        check(!isWritten) { "Cannot write symbol twice" }
        isWritten = true
        classData.writeToClassFile(true) // Class is always written right before metadata, so we can force writing
        val visitor = factory()
        actions.forEach { visitor.it() }
    }

    override var isWritten: Boolean = false
        private set

    override fun setPackagePrivate(): Unit = throw AssertionError("Should not be called")

    override fun hideFromJava() = throw AssertionError("Should not be called")

    override fun hideFromKotlin() = throw AssertionError("Should not be called")

    override val canQueue: Boolean
        get() = !isWritten

    override fun queueAction(action: AnnotationVisitor.() -> Unit) {
        check(!isWritten) { "Cannot queue action after symbol has been written" }
        actions += action
    }
}
