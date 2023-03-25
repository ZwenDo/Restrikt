package com.zwendo.restrikt.plugin.backend.visitor

import com.zwendo.restrikt.plugin.backend.ASM_VERSION
import com.zwendo.restrikt.plugin.backend.symbol.ClassData
import org.jetbrains.org.objectweb.asm.AnnotationVisitor
import org.jetbrains.org.objectweb.asm.Attribute
import org.jetbrains.org.objectweb.asm.ClassVisitor
import org.jetbrains.org.objectweb.asm.ModuleVisitor
import org.jetbrains.org.objectweb.asm.TypePath


internal class RestriktClassVisitor(private val context: ClassData) : ClassVisitor(ASM_VERSION) {

    override fun visitNestHost(nestHost: String?) = queue { visitNestHost(nestHost) }

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor {
        preVisitSymbolDeclarationAnnotation(descriptor, context)
        return annotationVisitor(context) {
            visitSymbolDeclarationAnnotation(descriptor, visible) { d, v -> visitAnnotation(d, v) }
        }
    }

    override fun visitModule(name: String?, access: Int, version: String?): ModuleVisitor {
        lateinit var moduleVisitor: ModuleVisitor
        queue { moduleVisitor = visitModule(name, access, version) }
        return RestriktModuleVisitor(context) { moduleVisitor }
    }

    override fun visitTypeAnnotation(
        typeRef: Int,
        typePath: TypePath?,
        descriptor: String?,
        visible: Boolean,
    ): AnnotationVisitor = annotationVisitor(context) {
        visitTypeAnnotation(typeRef, typePath, descriptor, visible)
    }

    override fun visitAttribute(attribute: Attribute?) = queue { visitAttribute(attribute) }

    override fun visitNestMember(nestMember: String?) = queue { visitNestMember(nestMember) }

    override fun visitPermittedSubclass(permittedSubclass: String?) =
        queue { visitPermittedSubclass(permittedSubclass) }

    override fun visitSource(source: String?, debug: String?) =
        throw AssertionError("Should not be called")

    override fun visitOuterClass(owner: String?, name: String?, descriptor: String?) =
        queue { visitOuterClass(owner, name, descriptor) }

    override fun visitInnerClass(name: String?, outerName: String?, innerName: String?, access: Int) =
        throw AssertionError("Should not be called")

    override fun visitRecordComponent(name: String?, descriptor: String?, signature: String?) =
        throw AssertionError("Should not be called")

    override fun visitField(access: Int, name: String, descriptor: String?, signature: String?, value: Any?) =
        throw AssertionError("Should not be called")

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?,
    ) = throw AssertionError("Should not be called")

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?,
    ) = throw AssertionError("Should not be called")

    override fun visitEnd() = queue { visitEnd() }

    private inline fun queue(crossinline action: ClassVisitor.() -> Unit) = context.queueAction { action() }

}
