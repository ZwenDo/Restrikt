package com.zwendo.restrikt.plugin.backend.symbol

import org.jetbrains.kotlin.backend.common.pop
import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.PropertyAccessorDescriptor
import org.jetbrains.org.objectweb.asm.AnnotationVisitor
import org.jetbrains.org.objectweb.asm.ClassVisitor
import org.jetbrains.org.objectweb.asm.FieldVisitor
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.RecordComponentVisitor

internal object RestriktClassBuildingContext {

    private val stack = SymbolDataStack()
    private val classNameStack = mutableListOf<String>()
    private val classNameToData = mutableMapOf<String, ClassData>()

    val currentClassData: ClassData
        get() = classNameToData[classNameStack.last()]!!

    val currentClassDataOrNull: ClassData?
        get() = classNameToData[classNameStack.lastOrNull()]

    fun methodData(
        descriptor: DeclarationDescriptor?,
        baseAccess: Int,
        factory: (Int) -> MethodVisitor,
    ): SymbolData<MethodVisitor> {
        setClassDescriptor(descriptor)
        return onSymbolCreation {
            if (descriptor is PropertyAccessorDescriptor) {
                PropertyAccessorData(currentClassData, descriptor, baseAccess, factory)
            } else {
                BasicMethodData(currentClassData, descriptor, baseAccess, factory)
            }
        }
    }

    fun fieldData(
        descriptor: DeclarationDescriptor?,
        baseAccess: Int,
        factory: (Int) -> FieldVisitor,
    ): SymbolData<FieldVisitor> {
        setClassDescriptor(descriptor)
        return onSymbolCreation { FieldData(currentClassData, descriptor, baseAccess, factory) }
    }

    fun metadataProxyData(factory: () -> AnnotationVisitor): SymbolData<AnnotationVisitor> {
        val data = MetadataProxyData(currentClassData, factory)
        stack.push(data)
        return data
    }

    fun recordComponentData(factory: () -> RecordComponentVisitor): SymbolData<RecordComponentVisitor> {
        val data = RecordComponentData(currentClassData) { factory() }
        stack.push(data)
        return data
    }

    fun createClassData(builder: ClassBuilder, name: String, baseAccess: Int, factory: (Int) -> ClassVisitor) {
        classNameStack += name
        classNameToData[name] = onSymbolCreation { ClassData(currentClassDataOrNull, builder, baseAccess, factory) }
    }

    fun classAccess(name: String, default: Int): Int = classNameToData[name]?.access ?: default

    /**
     * Called before writing a part of the class that does not require a [SymbolData] (e.g. innerClass call). This call
     * is intended to keep the writing order.
     */
    fun onStartWritingPartWithoutData() {
        stack.writeTop(false)
    }

    fun done() {
        stack.writeTop(true)
        classNameStack.pop()
    }

    private fun setClassDescriptor(descriptor: DeclarationDescriptor?) =
        classNameToData[classNameStack.last()]!!.setDescriptor(descriptor?.containingDeclaration)

    private inline fun <E, D : WritableSymbolData<E>> onSymbolCreation(factory: () -> D): D {
        val data = factory()
        stack.writeTop(false)
        stack.push(data)
        return data
    }

}
