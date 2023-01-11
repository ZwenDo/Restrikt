package com.zwendo.restrikt.plugin.backend.visitor

import com.zwendo.restrikt.plugin.backend.ASM_VERSION
import com.zwendo.restrikt.plugin.backend.symbol.SymbolData
import org.jetbrains.org.objectweb.asm.AnnotationVisitor
import org.jetbrains.org.objectweb.asm.Attribute
import org.jetbrains.org.objectweb.asm.Handle
import org.jetbrains.org.objectweb.asm.Label
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.TypePath


internal class RestriktMethodVisitor(
    private val context: SymbolData<MethodVisitor>,
) : MethodVisitor(ASM_VERSION) {


    override fun visitParameter(name: String?, access: Int) = queue { visitParameter(name, access) }

    override fun visitAnnotationDefault(): AnnotationVisitor =
        annotationVisitor(context) { visitAnnotationDefault() }

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor {
        preVisitSymbolDeclarationAnnotation(descriptor, context)
        return annotationVisitor(context) {
            visitSymbolDeclarationAnnotation(descriptor, visible) { d, v -> visitAnnotation(d, v) }
        }
    }

    override fun visitTypeAnnotation(
        typeRef: Int,
        typePath: TypePath?,
        descriptor: String?,
        visible: Boolean,
    ): AnnotationVisitor = annotationVisitor(context) {
        visitTypeAnnotation(typeRef, typePath, descriptor, visible)
    }

    override fun visitAnnotableParameterCount(parameterCount: Int, visible: Boolean) = queue {
        visitAnnotableParameterCount(parameterCount, visible)
    }

    override fun visitParameterAnnotation(parameter: Int, descriptor: String?, visible: Boolean): AnnotationVisitor =
        annotationVisitor(context) {
            visitParameterAnnotation(parameter, descriptor, visible)
        }

    override fun visitAttribute(attribute: Attribute?) = queue { visitAttribute(attribute) }

    override fun visitCode() = queue { visitCode() }

    override fun visitFrame(type: Int, numLocal: Int, local: Array<out Any>?, numStack: Int, stack: Array<out Any>?) =
        queue { visitFrame(type, numLocal, local, numStack, stack) }

    override fun visitInsn(opcode: Int) = queue { visitInsn(opcode) }

    override fun visitIntInsn(opcode: Int, operand: Int) = queue { visitIntInsn(opcode, operand) }

    override fun visitVarInsn(opcode: Int, `var`: Int) = queue { visitVarInsn(opcode, `var`) }

    override fun visitTypeInsn(opcode: Int, type: String?) = queue { visitTypeInsn(opcode, type) }

    override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, descriptor: String?) = queue {
        visitFieldInsn(opcode, owner, name, descriptor)
    }

    override fun visitMethodInsn(
        opcode: Int,
        owner: String?,
        name: String?,
        descriptor: String?,
        isInterface: Boolean,
    ) = queue { visitMethodInsn(opcode, owner, name, descriptor, isInterface) }

    override fun visitInvokeDynamicInsn(
        name: String?,
        descriptor: String?,
        bootstrapMethodHandle: Handle?,
        vararg bootstrapMethodArguments: Any?,
    ) = queue {
        visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, *bootstrapMethodArguments)
    }

    override fun visitJumpInsn(opcode: Int, label: Label?) = queue { visitJumpInsn(opcode, label) }

    override fun visitLabel(label: Label?) = queue { visitLabel(label) }

    override fun visitLdcInsn(value: Any?) = queue { visitLdcInsn(value) }

    override fun visitIincInsn(`var`: Int, increment: Int) =
        queue { visitIincInsn(`var`, increment) }

    override fun visitTableSwitchInsn(min: Int, max: Int, dflt: Label?, vararg labels: Label?) = queue {
        visitTableSwitchInsn(min, max, dflt, *labels)
    }

    override fun visitLookupSwitchInsn(dflt: Label?, keys: IntArray?, labels: Array<out Label>?) = queue {
        visitLookupSwitchInsn(dflt, keys, labels)
    }

    override fun visitMultiANewArrayInsn(descriptor: String?, numDimensions: Int) = queue {
        visitMultiANewArrayInsn(descriptor, numDimensions)
    }

    override fun visitInsnAnnotation(
        typeRef: Int,
        typePath: TypePath?,
        descriptor: String?,
        visible: Boolean,
    ): AnnotationVisitor = annotationVisitor(context) {
        visitInsnAnnotation(typeRef, typePath, descriptor, visible)
    }

    override fun visitTryCatchBlock(start: Label?, end: Label?, handler: Label?, type: String?) = queue {
        visitTryCatchBlock(start, end, handler, type)
    }

    override fun visitTryCatchAnnotation(
        typeRef: Int,
        typePath: TypePath?,
        descriptor: String?,
        visible: Boolean,
    ): AnnotationVisitor = annotationVisitor(context) {
        visitTryCatchAnnotation(typeRef, typePath, descriptor, visible)
    }

    override fun visitLocalVariable(
        name: String?,
        descriptor: String?,
        signature: String?,
        start: Label?,
        end: Label?,
        index: Int,
    ) = queue { visitLocalVariable(name, descriptor, signature, start, end, index) }

    override fun visitLocalVariableAnnotation(
        typeRef: Int,
        typePath: TypePath?,
        start: Array<out Label>?,
        end: Array<out Label>?,
        index: IntArray?,
        descriptor: String?,
        visible: Boolean,
    ): AnnotationVisitor = annotationVisitor(context) {
        visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible)
    }

    override fun visitLineNumber(line: Int, start: Label?) = queue { visitLineNumber(line, start) }

    override fun visitMaxs(maxStack: Int, maxLocals: Int) = queue {
        visitMaxs(maxStack, maxLocals)
    }

    override fun visitEnd() = queue { visitEnd() }

    private inline fun queue(crossinline action: MethodVisitor.() -> Unit) = context.queueAction { action() }

}
