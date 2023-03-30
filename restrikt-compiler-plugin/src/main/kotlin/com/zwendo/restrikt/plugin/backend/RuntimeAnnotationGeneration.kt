package com.zwendo.restrikt.plugin.backend

import com.zwendo.restrikt.annotation.HideFromJava
import com.zwendo.restrikt.annotation.HideFromKotlin
import com.zwendo.restrikt.annotation.PackagePrivate
import com.zwendo.restrikt.annotation.RestriktRetention
import com.zwendo.restrikt.plugin.frontend.PluginConfiguration
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.runtime.structure.desc
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.org.objectweb.asm.AnnotationVisitor

internal fun generateRuntimeAnnotation(
    descriptor: DeclarationDescriptor?,
    desc: String,
    visible: Boolean,
    visitorFactory: (String, Boolean) -> AnnotationVisitor,
): AnnotationVisitor {
    // try to find the annotation data for the given descriptor
    val annotationData = findAnnotationData(desc) ?: return visitorFactory(desc, visible)

    // try to find the annotation descriptor for the given data
    val annotationDescriptor = descriptor?.annotations?.findAnnotation(annotationData.fqName)
        ?: return visitorFactory(desc, visible)

    // retrieve the annotation retention from the descriptor
    val retention = annotationDescriptor.allValueArguments[annotationData.retentionName]?.let {
        @Suppress("UNCHECKED_CAST")
        val value = (it.value as Pair<*, Name>).second.asString()
        RestriktRetention.valueOf(value)
    } ?: RestriktRetention.DEFAULT

    val pluginPolicy = annotationData.annotationConfig.generationPolicy!!
    if (
        retention == RestriktRetention.SOURCE
        || (retention == RestriktRetention.DEFAULT && !pluginPolicy.writeToClassFile)
    ) {
        return annotationRemovingVisitor
    }

    val isRuntime = retention == RestriktRetention.RUNTIME
                || (retention == RestriktRetention.DEFAULT && pluginPolicy.isRuntime)

    val original = visitorFactory(desc, isRuntime)
    return RestriktAnnotationVisitor(annotationData, original)
}

private fun findAnnotationData(descriptor: String): AnnotationData? = when (descriptor) {
    HIDE_FROM_JAVA_DESC -> HIDE_FROM_JAVA_DATA
    HIDE_FROM_KOTLIN_DESC -> HIDE_FROM_KOTLIN_DATA
    PACKAGE_PRIVATE_DESC -> PACKAGE_PRIVATE_DATA
    else -> null
}


private class AnnotationData(
    annotationClass: Class<*>,
    val annotationConfig: PluginConfiguration.AnnotationConfiguration,
    val reasonName: String,
    retentionNameString: String,
) {

    val fqName: FqName = FqName(annotationClass.canonicalName)

    val retentionName = Name.identifier(retentionNameString)

}

private val HIDE_FROM_KOTLIN_DATA = AnnotationData(
    HideFromKotlin::class.java,
    PluginConfiguration.hideFromKotlin,
    HideFromKotlin::reason.name,
    HideFromKotlin::retention.name,
)

private val HIDE_FROM_JAVA_DATA = AnnotationData(
    HideFromJava::class.java,
    PluginConfiguration.hideFromJava,
    HideFromJava::reason.name,
    HideFromJava::retention.name,
)

private val PACKAGE_PRIVATE_DATA = AnnotationData(
    PackagePrivate::class.java,
    PluginConfiguration.packagePrivate,
    PackagePrivate::reason.name,
    PackagePrivate::retention.name,
)

private val HIDE_FROM_JAVA_DESC = HideFromJava::class.java.desc

private val PACKAGE_PRIVATE_DESC = PackagePrivate::class.java.desc

private class RestriktAnnotationVisitor(
    private val data: AnnotationData,
    original: AnnotationVisitor,
) : AnnotationVisitor(ASM_VERSION, original) {

    private var hasReason = false

    override fun visit(name: String?, value: Any?) {
        if (name == data.reasonName) hasReason = true
        super.visit(name, value)
    }

    override fun visitEnd() {
        if (!hasReason) {
            data.annotationConfig.defaultReason?.let {
                visit(data.reasonName, it)
            }
        }
        super.visitEnd()
    }

}

private val annotationRemovingVisitor = object : AnnotationVisitor(ASM_VERSION) {}
