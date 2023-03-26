package com.zwendo.restrikt.plugin.backend.visitor

import com.zwendo.restrikt.annotation.HideFromJava
import org.jetbrains.kotlin.descriptors.runtime.structure.desc
import org.jetbrains.org.objectweb.asm.AnnotationVisitor
import org.jetbrains.org.objectweb.asm.Attribute
import org.jetbrains.org.objectweb.asm.ClassVisitor
import org.jetbrains.org.objectweb.asm.FieldVisitor
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.ModuleVisitor
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.RecordComponentVisitor
import org.jetbrains.org.objectweb.asm.TypePath


internal class RestriktClassVisitor(
    original: ClassVisitor,
    private val definitionLambda: ((Int) -> Int) -> Unit,
    written: Boolean,
) : ClassVisitor(ASM_VERSION, original) {

    private val writingQueue = ArrayList<() -> Unit>()

    var written: Boolean = written
        private set

    fun write() {
        if (written) return
        definitionLambda { it }
        writingQueue.forEach { it() }
        written = true
    }

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
        if (descriptor == HIDE_FROM_JAVA_DESC) { // TODO NO
            written = true
            definitionLambda { it or Opcodes.ACC_SYNTHETIC }
        }
        return if (written) {
            super.visitAnnotation(descriptor, visible)
        } else {
            RestriktAnnotationVisitor(writingQueue::add) {
                super.visitAnnotation(descriptor, visible)
            }
        }
    }

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?,
    ) = if (written) {
        super.visit(version, access, name, signature, superName, interfaces)
    } else {
        writingQueue += { super.visit(version, access, name, signature, superName, interfaces) }
    }

    override fun visitSource(source: String?, debug: String?) = if (written) {
        super.visitSource(source, debug)
    } else {
        writingQueue += { super.visitSource(source, debug) }
    }

    override fun visitModule(name: String?, access: Int, version: String?): ModuleVisitor {
        return super.visitModule(name, access, version)
    }

    override fun visitNestHost(nestHost: String?) = if (written) {
        super.visitNestHost(nestHost)
    } else {
        writingQueue += { super.visitNestHost(nestHost) }
    }

    override fun visitOuterClass(owner: String?, name: String?, descriptor: String?) = if (written) {
        super.visitOuterClass(owner, name, descriptor)
    } else {
        writingQueue += { super.visitOuterClass(owner, name, descriptor) }
    }

    override fun visitTypeAnnotation(
        typeRef: Int,
        typePath: TypePath?,
        descriptor: String?,
        visible: Boolean,
    ): AnnotationVisitor {
        return RestriktAnnotationVisitor(writingQueue::add) {
            super.visitTypeAnnotation(typeRef, typePath, descriptor, visible)
        }
    }

    override fun visitAttribute(attribute: Attribute?) = if (written) {
        super.visitAttribute(attribute)
    } else {
        writingQueue += { super.visitAttribute(attribute) }
    }

    override fun visitNestMember(nestMember: String?) = if (written) {
        super.visitNestMember(nestMember)
    } else {
        writingQueue += { super.visitNestMember(nestMember) }
    }

    override fun visitPermittedSubclass(permittedSubclass: String?) = if (written) {
        super.visitPermittedSubclass(permittedSubclass)
    } else {
        writingQueue += { super.visitPermittedSubclass(permittedSubclass) }
    }

    override fun visitInnerClass(name: String?, outerName: String?, innerName: String?, access: Int) = if (written) {
        super.visitInnerClass(name, outerName, innerName, access)
    } else {
        writingQueue += { super.visitInnerClass(name, outerName, innerName, access) }
    }

    override fun visitRecordComponent(name: String?, descriptor: String?, signature: String?): RecordComponentVisitor =
        throw AssertionError("Should not be called")

    override fun visitField(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        value: Any?,
    ): FieldVisitor = throw AssertionError("Should not be called")

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?,
    ): MethodVisitor = throw AssertionError("Should not be called")

    override fun visitEnd() = if (written) {
        super.visitEnd()
    } else {
        writingQueue += { super.visitEnd() }
        write()
    }

    private companion object {

        val HIDE_FROM_JAVA_DESC = HideFromJava::class.java.desc

    }

}
