package com.zwendo.restrikt.plugin.backend.visitor

import com.zwendo.restrikt.plugin.backend.RestriktContext as context
import com.zwendo.restrikt.plugin.backend.ASM_VERSION
import org.jetbrains.org.objectweb.asm.AnnotationVisitor
import org.jetbrains.org.objectweb.asm.Attribute
import org.jetbrains.org.objectweb.asm.Handle
import org.jetbrains.org.objectweb.asm.Label
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.TypePath


internal class RestriktMethodVisitor(
    private val signature: String,
    factory: () -> MethodVisitor,
) : MethodVisitor(ASM_VERSION) {

    private val original: MethodVisitor by lazy { factory() }

    override fun visitParameter(name: String?, access: Int) = context.addAction {
        original.visitParameter(name, access)
    }

    override fun visitAnnotationDefault(): AnnotationVisitor {
        lateinit var visitor: AnnotationVisitor
        context.addAction { visitor = original.visitAnnotationDefault() }
        return RestriktAnnotationVisitor { visitor }
    }

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor {
        lateinit var visitor: AnnotationVisitor

        checkHideFromJava(descriptor) {
            context.currentClass?.makeSynthetic(signature)
        }

        context.addAction {
            visitor = visitSymbolDeclarationAnnotation(descriptor, visible, original::visitAnnotation)
        }

        return RestriktAnnotationVisitor { visitor }
    }

    override fun visitTypeAnnotation(
        typeRef: Int,
        typePath: TypePath?,
        descriptor: String?,
        visible: Boolean,
    ): AnnotationVisitor {
        lateinit var visitor: AnnotationVisitor
        context.addAction { visitor = original.visitTypeAnnotation(typeRef, typePath, descriptor, visible) }
        return RestriktAnnotationVisitor { visitor }
    }

    override fun visitAnnotableParameterCount(parameterCount: Int, visible: Boolean) = context.addAction {
        original.visitAnnotableParameterCount(parameterCount, visible)
    }

    override fun visitParameterAnnotation(parameter: Int, descriptor: String?, visible: Boolean): AnnotationVisitor {
        lateinit var visitor: AnnotationVisitor
        context.addAction { visitor = original.visitParameterAnnotation(parameter, descriptor, visible) }
        return RestriktAnnotationVisitor { visitor }
    }

    override fun visitAttribute(attribute: Attribute?) = context.addAction { original.visitAttribute(attribute) }

    override fun visitCode() = context.addAction { original.visitCode() }

    override fun visitFrame(type: Int, numLocal: Int, local: Array<out Any>?, numStack: Int, stack: Array<out Any>?) =
        context.addAction { original.visitFrame(type, numLocal, local, numStack, stack) }

    override fun visitInsn(opcode: Int) = context.addAction { original.visitInsn(opcode) }

    override fun visitIntInsn(opcode: Int, operand: Int) = context.addAction { original.visitIntInsn(opcode, operand) }

    override fun visitVarInsn(opcode: Int, `var`: Int) = context.addAction { original.visitVarInsn(opcode, `var`) }

    override fun visitTypeInsn(opcode: Int, type: String?) = context.addAction { original.visitTypeInsn(opcode, type) }

    override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, descriptor: String?) = context.addAction {
        original.visitFieldInsn(opcode, owner, name, descriptor)
    }

    override fun visitMethodInsn(
        opcode: Int,
        owner: String?,
        name: String?,
        descriptor: String?,
        isInterface: Boolean,
    ) = context.addAction { original.visitMethodInsn(opcode, owner, name, descriptor, isInterface) }

    override fun visitInvokeDynamicInsn(
        name: String?,
        descriptor: String?,
        bootstrapMethodHandle: Handle?,
        vararg bootstrapMethodArguments: Any?,
    ) = context.addAction {
        original.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, *bootstrapMethodArguments)
    }

    override fun visitJumpInsn(opcode: Int, label: Label?) = context.addAction { original.visitJumpInsn(opcode, label) }

    override fun visitLabel(label: Label?) = context.addAction { original.visitLabel(label) }

    override fun visitLdcInsn(value: Any?) = context.addAction { original.visitLdcInsn(value) }

    override fun visitIincInsn(`var`: Int, increment: Int) =
        context.addAction { original.visitIincInsn(`var`, increment) }

    override fun visitTableSwitchInsn(min: Int, max: Int, dflt: Label?, vararg labels: Label?) = context.addAction {
        original.visitTableSwitchInsn(min, max, dflt, *labels)
    }

    override fun visitLookupSwitchInsn(dflt: Label?, keys: IntArray?, labels: Array<out Label>?) = context.addAction {
        original.visitLookupSwitchInsn(dflt, keys, labels)
    }

    override fun visitMultiANewArrayInsn(descriptor: String?, numDimensions: Int) = context.addAction {
        original.visitMultiANewArrayInsn(descriptor, numDimensions)
    }

    override fun visitInsnAnnotation(
        typeRef: Int,
        typePath: TypePath?,
        descriptor: String?,
        visible: Boolean,
    ): AnnotationVisitor {
        lateinit var visitor: AnnotationVisitor
        context.addAction { visitor = original.visitInsnAnnotation(typeRef, typePath, descriptor, visible) }
        return RestriktAnnotationVisitor { visitor }
    }

    override fun visitTryCatchBlock(start: Label?, end: Label?, handler: Label?, type: String?) = context.addAction {
        original.visitTryCatchBlock(start, end, handler, type)
    }

    override fun visitTryCatchAnnotation(
        typeRef: Int,
        typePath: TypePath?,
        descriptor: String?,
        visible: Boolean,
    ): AnnotationVisitor {
        lateinit var visitor: AnnotationVisitor
        context.addAction { visitor = original.visitTryCatchAnnotation(typeRef, typePath, descriptor, visible) }
        return RestriktAnnotationVisitor { visitor }
    }

    override fun visitLocalVariable(
        name: String?,
        descriptor: String?,
        signature: String?,
        start: Label?,
        end: Label?,
        index: Int,
    ) = context.addAction { original.visitLocalVariable(name, descriptor, signature, start, end, index) }

    override fun visitLocalVariableAnnotation(
        typeRef: Int,
        typePath: TypePath?,
        start: Array<out Label>?,
        end: Array<out Label>?,
        index: IntArray?,
        descriptor: String?,
        visible: Boolean,
    ): AnnotationVisitor {
        lateinit var visitor: AnnotationVisitor
        context.addAction {
            visitor = original.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible)
        }
        return RestriktAnnotationVisitor { visitor }
    }

    override fun visitLineNumber(line: Int, start: Label?) = context.addAction { original.visitLineNumber(line, start) }

    override fun visitMaxs(maxStack: Int, maxLocals: Int) = context.addAction {
        original.visitMaxs(maxStack, maxLocals)
    }

    override fun visitEnd() = context.addAction { original.visitEnd() }
}
