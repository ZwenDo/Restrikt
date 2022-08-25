package com.zwendo.restrikt.plugin.backend.visitor

import com.zwendo.restrikt.plugin.backend.RestriktContext as context
import com.zwendo.restrikt.plugin.backend.ASM_VERSION
import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata
import org.jetbrains.org.objectweb.asm.AnnotationVisitor


internal class RestriktMetadataVisitor(original: () -> AnnotationVisitor) : AnnotationVisitor(ASM_VERSION) {

    private var k: Int? = null
    private var mv: IntArray? = null
    private var xi: Int? = null
    private val d1 = mutableListOf<String>()
    private val d2 = mutableListOf<String>()

    private lateinit var lastKey: String
    private val aux = Aux { original() }

    override fun visit(name: String?, value: Any?) = aux.visit(name, value)

    override fun visitEnum(name: String?, descriptor: String?, value: String?) = aux.visitEnum(name, descriptor, value)

    override fun visitAnnotation(name: String?, descriptor: String?) = aux.visitAnnotation(name, descriptor)

    override fun visitArray(name: String?) = aux.visitArray(name)

    override fun visitEnd() {
        aux.visitEnd()

        val d1Array = if (d1.isEmpty()) null else d1.toTypedArray()
        val d2Array = if (d2.isEmpty()) null else d2.toTypedArray()
        val header = KotlinClassHeader(k, mv, d1Array, d2Array, null, null, xi)
        val data = KotlinClassMetadata.read(header) ?: return
        context.storeClass(data)
    }

    private inner class Aux(factory: () -> AnnotationVisitor) : AnnotationVisitor(ASM_VERSION) {

        private val original: AnnotationVisitor by lazy { factory() }

        override fun visit(name: String?, value: Any?) {
            lastKey = name ?: lastKey
            setValue(value)
            context.addAction {
                original.visit(name, value)
            }
        }

        override fun visitEnum(name: String?, descriptor: String?, value: String?) {
            // should not be called
            context.addAction { original.visitEnum(name, descriptor, value) }
        }

        override fun visitAnnotation(name: String?, descriptor: String?): AnnotationVisitor {
            // should not be called
            lateinit var visitor: AnnotationVisitor
            context.addAction { visitor = original.visitAnnotation(name, descriptor) }
            return Aux { visitor }
        }

        override fun visitArray(name: String?): AnnotationVisitor {
            lastKey = name ?: lastKey
            lateinit var visitor: AnnotationVisitor
            context.addAction { visitor = original.visitArray(name) }
            return Aux { visitor }
        }

        override fun visitEnd() = context.addAction { original.visitEnd() }

        private fun setValue(value: Any?) {
            when (lastKey) {
                "mv" -> mv = value as IntArray
                "k" -> k = value as Int
                "xi" -> xi = value as Int
                "d1" -> d1 += value as String
                "d2" -> d2 += value as String
            }
        }

    }
}
