package com.zwendo.restrikt.plugin.backend

import com.zwendo.restrikt.plugin.backend.symbol.RestriktClassBuildingContext
import com.zwendo.restrikt.plugin.backend.visitor.RestriktClassVisitor
import com.zwendo.restrikt.plugin.backend.visitor.RestriktClassVisitorProxy
import com.zwendo.restrikt.plugin.backend.visitor.RestriktFieldVisitor
import com.zwendo.restrikt.plugin.backend.visitor.RestriktMetadataVisitor
import com.zwendo.restrikt.plugin.backend.visitor.RestriktMethodVisitor
import com.zwendo.restrikt.plugin.backend.visitor.RestriktRecordComponentVisitor
import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.DelegatingClassBuilder
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.org.objectweb.asm.AnnotationVisitor
import org.jetbrains.org.objectweb.asm.ClassVisitor
import org.jetbrains.org.objectweb.asm.FieldVisitor
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.RecordComponentVisitor


internal class RestriktClassBuilder(private val original: ClassBuilder) : DelegatingClassBuilder() {

    override fun getDelegate(): ClassBuilder = original

    override fun getVisitor(): ClassVisitor {
        val classData = RestriktClassBuildingContext.currentClassData
        // the following if aims to avoid infinite queueing, in the case of the RestriktClassVisitor tries to call
        // methods from the original visitor (e.g. avoid infinite annotation queueing)
        return if (classData.isWritten) { // most common case, class containing at least 1 symbol
            RestriktClassVisitorProxy(classData, original.visitor)
        } else { // empty interface/annotation class
            RestriktClassVisitor(classData)
        }
    }

    override fun newMethod(
        origin: JvmDeclarationOrigin,
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        exceptions: Array<out String>?,
    ): MethodVisitor {
        val data = RestriktClassBuildingContext.methodData(origin.descriptor, access) {
            super.newMethod(origin, it, name, desc, signature, exceptions)
        }
        return RestriktMethodVisitor(data)
    }

    override fun newField(
        origin: JvmDeclarationOrigin,
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        value: Any?,
    ): FieldVisitor {
        val data = RestriktClassBuildingContext.fieldData(origin.descriptor, access) {
            super.newField(origin, it, name, desc, signature, value)
        }
        return RestriktFieldVisitor(data)
    }

    override fun visitInnerClass(name: String, outerName: String?, innerName: String?, access: Int) {
        RestriktClassBuildingContext.onStartWritingPartWithoutData()
        val actualAccess = RestriktClassBuildingContext.classAccess(name, access)
        super.visitInnerClass(name, outerName, innerName, actualAccess)
    }

    override fun defineClass(
        origin: PsiElement?,
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String,
        interfaces: Array<out String>,
    ) = RestriktClassBuildingContext.createClassData(original, name, access) {
        super.defineClass(origin, version, it, name, signature, superName, interfaces)
        original.visitor
    }

    override fun newRecordComponent(name: String, desc: String, signature: String?): RecordComponentVisitor {
        val data = RestriktClassBuildingContext.recordComponentData {
            super.newRecordComponent(name, desc, signature)
        }
        return RestriktRecordComponentVisitor(data)
    }

    override fun newAnnotation(desc: String, visible: Boolean): AnnotationVisitor {
        // most common case, class containing at least 1 symbol and has already been written, so we do not need to
        // parse the metadata
        if (RestriktClassBuildingContext.currentClassData.isWritten) {
            return super.newAnnotation(desc, visible)
        }

        // we must parse the metadata to gather the information about the class, as it has an empty body, meaning that
        // we did not have the possibility to retrieve its descriptor
        val data = RestriktClassBuildingContext.metadataProxyData {
            super.newAnnotation(desc, visible)
        }
        return RestriktMetadataVisitor(RestriktClassBuildingContext.currentClassData, data)
    }

    override fun done(generateSmapCopyToAnnotation: Boolean) {
        RestriktClassBuildingContext.done()
        super.done(generateSmapCopyToAnnotation)
    }

}
