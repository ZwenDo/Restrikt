package com.zwendo.restrikt.plugin.backend.visitor

import com.zwendo.restrikt.plugin.backend.ASM_VERSION
import com.zwendo.restrikt.plugin.backend.symbol.ClassData
import com.zwendo.restrikt.plugin.backend.symbol.SymbolData
import kotlinx.metadata.Flag
import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata
import org.jetbrains.org.objectweb.asm.AnnotationVisitor


internal class RestriktMetadataVisitor(
    private val classData: ClassData,
    private val context: SymbolData<AnnotationVisitor>,
) : AnnotationVisitor(ASM_VERSION) {

    private var k: Int? = null
    private var mv: IntArray? = null
    private var xi: Int? = null
    private val d1 = mutableListOf<String>()
    private val d2 = mutableListOf<String>()

    private lateinit var lastKey: String

    override fun visit(name: String?, value: Any?) {
        lastKey = name ?: lastKey
        setValue(value)
        queue { visit(name, value) }
    }

    // should not be called
    override fun visitEnum(name: String?, descriptor: String?, value: String?) =
        queue { visitEnum(name, descriptor, value) }

    // should not be called
    override fun visitAnnotation(name: String?, descriptor: String?): AnnotationVisitor = annotationVisitor(
        context,
        { _, v -> Aux(v) }
    ) {
        visitAnnotation(name, descriptor)
    }

    override fun visitArray(name: String?): AnnotationVisitor {
        lastKey = name ?: lastKey
        return annotationVisitor(
            context,
            { _, v -> Aux(v) }
        ) {
            visitArray(name)
        }
    }

    override fun visitEnd() {
        queue { visitEnd() }

        val d1Array = if (d1.isEmpty()) null else d1.toTypedArray()
        val d2Array = if (d2.isEmpty()) null else d2.toTypedArray()
        val header = KotlinClassHeader(k, mv, d1Array, d2Array, null, null, xi)

        val data = KotlinClassMetadata.read(header) ?: return
        if (data !is KotlinClassMetadata.Class) return

        val clazz = data.toKmClass()
        val isInternal = Flag.Common.IS_INTERNAL(clazz.flags)
        if (isInternal) {
            classData.setInternal()
        }
    }

    private inline fun queue(crossinline action: AnnotationVisitor.() -> Unit) = context.queueAction { action() }

    private fun setValue(value: Any?) {
        when (lastKey) {
            "mv" -> mv = value as IntArray
            "k" -> k = value as Int
            "xi" -> xi = value as Int
            "d1" -> d1 += value as String
            "d2" -> d2 += value as String
        }
    }

    private inner class Aux(private val factory: () -> AnnotationVisitor) : AnnotationVisitor(ASM_VERSION) {

        private val original: AnnotationVisitor by lazy { factory() }

        override fun visit(name: String?, value: Any?) {
            lastKey = name ?: lastKey
            setValue(value)
            queue { original.visit(name, value) }
        }

        // should not be called
        override fun visitEnum(name: String?, descriptor: String?, value: String?) =
            queue { original.visitEnum(name, descriptor, value) }

        // should not be called
        override fun visitAnnotation(name: String?, descriptor: String?): AnnotationVisitor = annotationVisitor(
            context,
            { _, v -> Aux(v) }
        ) {
            original.visitAnnotation(name, descriptor)
        }

        override fun visitArray(name: String?): AnnotationVisitor {
            lastKey = name ?: lastKey
            return annotationVisitor(
                context,
                { _, v -> Aux(v) }
            ) {
                original.visitArray(name)
            }
        }

        override fun visitEnd() = queue { original.visitEnd() }

    }

}
