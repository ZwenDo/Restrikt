package com.zwendo.restrikt.plugin.backend.visitor

import com.zwendo.restrikt.plugin.backend.RestriktContext as context
import com.zwendo.restrikt.plugin.backend.ASM_VERSION
import org.jetbrains.org.objectweb.asm.AnnotationVisitor
import org.jetbrains.org.objectweb.asm.Attribute
import org.jetbrains.org.objectweb.asm.ClassVisitor
import org.jetbrains.org.objectweb.asm.FieldVisitor
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.ModuleVisitor
import org.jetbrains.org.objectweb.asm.RecordComponentVisitor
import org.jetbrains.org.objectweb.asm.TypePath


internal class RestriktClassVisitor(factory: () -> ClassVisitor) : ClassVisitor(ASM_VERSION) {

    private val original: ClassVisitor by lazy { factory() }

    override fun visitNestHost(nestHost: String?) = context.addAction { original.visitNestHost(nestHost) }

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor {
        lateinit var visitor: AnnotationVisitor

        checkHideFromJava(descriptor) { context.getClass(context.currentClassName)?.forceSynthetic() }

        context.addAction {
            visitor = visitSymbolDeclarationAnnotation(
                descriptor,
                visible,
                original::visitAnnotation
            )
        }

        return RestriktAnnotationVisitor { visitor }
    }

    override fun visitModule(name: String?, access: Int, version: String?): ModuleVisitor {
        lateinit var visitor: ModuleVisitor
        context.addAction { visitor = original.visitModule(name, access, version) }
        return RestriktModuleVisitor { visitor }
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

    override fun visitAttribute(attribute: Attribute?) = context.addAction { original.visitAttribute(attribute) }

    override fun visitNestMember(nestMember: String?) = context.addAction { original.visitNestMember(nestMember) }

    override fun visitPermittedSubclass(permittedSubclass: String?) = context.addAction {
        original.visitPermittedSubclass(permittedSubclass)
    }

    override fun visitSource(source: String?, debug: String?) = context.addAction {
        original.visitSource(source, debug)
    }

    override fun visitOuterClass(owner: String?, name: String?, descriptor: String?) = context.addAction {
        original.visitOuterClass(owner, name, descriptor)
    }

    override fun visitInnerClass(name: String?, outerName: String?, innerName: String?, access: Int) =
        context.addAction { original.visitInnerClass(name, outerName, innerName, access) }

    override fun visitRecordComponent(name: String?, descriptor: String?, signature: String?): RecordComponentVisitor {
        lateinit var visitor: RecordComponentVisitor
        context.addAction { visitor = original.visitRecordComponent(name, descriptor, signature) }
        return RestriktRecordComponentVisitor { visitor }
    }

    override fun visitField(
        access: Int,
        name: String,
        descriptor: String?,
        signature: String?,
        value: Any?,
    ): FieldVisitor {
        lateinit var visitor: FieldVisitor
        context.addAction { visitor = original.visitField(access, name, descriptor, signature, value) }
        return RestriktFieldVisitor(name) { visitor }
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?,
    ): MethodVisitor {
        lateinit var visitor: MethodVisitor
        context.addAction { visitor = original.visitMethod(access, name, descriptor, signature, exceptions) }
        return RestriktMethodVisitor(name, descriptor) { visitor }
    }

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?,
    ) {
        context.addAction { original.visit(version, access, name, signature, superName, interfaces) }
    }

    override fun visitEnd() = context.addAction { original.visitEnd() }

}
