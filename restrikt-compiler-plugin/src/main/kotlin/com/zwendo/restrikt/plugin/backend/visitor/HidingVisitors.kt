package com.zwendo.restrikt.plugin.backend.visitor

import com.zwendo.restrikt.plugin.backend.Constants
import org.jetbrains.kotlin.descriptors.runtime.structure.desc
import org.jetbrains.org.objectweb.asm.AnnotationVisitor

internal class HideFromKotlinVisitor(
    original: AnnotationVisitor,
    visitorFactory: (String, Boolean) -> AnnotationVisitor,
) : RestriktAnnotationVisitor(original, visitorFactory) {

    private companion object {

        @JvmField
        val DEPRECATED_DESC = Deprecated::class.java.desc

        @JvmField
        val DEPRECATION_LEVEL_DESC = DeprecationLevel::class.java.desc
    }

    override fun visitEnd() {
        av.visitEnd()
        val visitor = visitorFactory(DEPRECATED_DESC, false)
        visitor.visit("message", "hidden to kotlin")
        visitor.visitEnum("level", DEPRECATION_LEVEL_DESC, DeprecationLevel.HIDDEN.toString())
    }

}


internal abstract class RestriktAnnotationVisitor(
    original: AnnotationVisitor,
    protected val visitorFactory: (String, Boolean) -> AnnotationVisitor,
) : AnnotationVisitor(Constants.ASM_VERSION, original) {

    private var visitedReason: Boolean = false
    private var visitedAlternative: Boolean = false

    final override fun visit(name: String?, value: Any?) = when (name) {
        Constants.REASON_PARAMETER_NAME -> {
            if (visitedReason) {
                TODO("throw")
            }
            visitedReason = true
            av.visit(name, value)
        }
        Constants.ALTERNATIVE_PARAMETER_NAME -> {
            if (visitedAlternative) {
                TODO("throw")
            }
            visitedAlternative = true
            av.visit(name, value)
        }
        else -> TODO("throw")
    }

    final override fun visitAnnotation(name: String?, desc: String?): AnnotationVisitor {
        TODO("throw")
    }

    final override fun visitArray(name: String?): AnnotationVisitor {
        TODO("throw")
    }

    final override fun visitEnum(name: String?, descriptor: String?, value: String?) {
        TODO("throw")
    }

    abstract override fun visitEnd()

}
