@file:OptIn(RestriktGeneratedAnnotation::class)

package com.zwendo.restrikt.plugin.backend.visitor

import com.zwendo.restrikt.annotation.AnnotationGeneration
import com.zwendo.restrikt.annotation.HideFromJava
import com.zwendo.restrikt.annotation.HideFromJavaMarker
import com.zwendo.restrikt.annotation.HideFromKotlin
import com.zwendo.restrikt.annotation.HideFromKotlinMarker
import com.zwendo.restrikt.annotation.PackagePrivate
import com.zwendo.restrikt.annotation.PackagePrivateMarker
import com.zwendo.restrikt.annotation.RestriktGeneratedAnnotation
import com.zwendo.restrikt.plugin.backend.HIDE_FROM_JAVA_FQNAME
import com.zwendo.restrikt.plugin.backend.HIDE_FROM_KOTLIN_FQNAME
import com.zwendo.restrikt.plugin.backend.PACKAGE_PRIVATE_FQNAME
import com.zwendo.restrikt.plugin.frontend.PluginConfiguration
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.descriptors.runtime.structure.desc
import org.jetbrains.kotlin.name.Name
import org.jetbrains.org.objectweb.asm.AnnotationVisitor
import org.jetbrains.org.objectweb.asm.Opcodes

internal const val ASM_VERSION = Opcodes.ASM9

internal fun generateMarkers(
    descriptor: DeclarationDescriptor?,
    classDescriptor: DeclarationDescriptor?,
    visitorFactory: (String, Boolean) -> AnnotationVisitor,
) {
    if (descriptor == null) return
    var hasHideFromKotlin = false
    descriptor.annotations.forEach {
        when (it.fqName) {
            HIDE_FROM_KOTLIN_FQNAME -> generateMarker(it, HIDE_FROM_KOTLIN_DATA, visitorFactory).also {
                hasHideFromKotlin = true
            }
            HIDE_FROM_JAVA_FQNAME -> generateMarker(it, HIDE_FROM_JAVA_DATA, visitorFactory)
            PACKAGE_PRIVATE_FQNAME -> generateMarker(it, PACKAGE_PRIVATE_DATA, visitorFactory)
        }
    }

    if (hasHideFromKotlin) return
    val desc = classDescriptor?.annotations?.findAnnotation(HIDE_FROM_KOTLIN_FQNAME) ?: return
    generateMarker(desc, HIDE_FROM_KOTLIN_DATA, visitorFactory)
}

private fun generateMarker(
    descriptor: AnnotationDescriptor,
    data: AnnotationData,
    visitorFactory: (String, Boolean) -> AnnotationVisitor,
) {
    var message: String? = null
    var retention: AnnotationGeneration = AnnotationGeneration.DEFAULT
    descriptor.allValueArguments.forEach { (n, v) ->
        when (n.asString()) {
            data.reasonName -> message = v.value.toString()
            data.retentionName -> {
                @Suppress("UNCHECKED_CAST")
                val value = (v.value as Pair<*, Name>).second.asString()
                retention = AnnotationGeneration.valueOf(value)
            }
        }
    }

    if (
        retention == AnnotationGeneration.NONE
        || (retention == AnnotationGeneration.DEFAULT && !data.annotationConfig.retention.writeToClassFile)
    ) {
        return // don't write the annotation
    }

    val isRuntime = retention == AnnotationGeneration.RUNTIME
                || (retention == AnnotationGeneration.DEFAULT && data.annotationConfig.retention.isRuntime)

    visitorFactory(data.generatedAnnotationDescriptor, isRuntime).apply {
        val actualMessage = message ?: data.annotationConfig.defaultReason
        actualMessage?.let { visit(data.reasonName, it) }
        visitEnd()
    }
}

private class AnnotationData(
    val generatedAnnotationDescriptor: String,
    val annotationConfig: PluginConfiguration.AnnotationConfiguration,
    val reasonName: String,
    val retentionName: String,
)

private val HIDE_FROM_KOTLIN_DATA = AnnotationData(
    HideFromKotlinMarker::class.java.desc,
    PluginConfiguration.hideFromKotlin,
    HideFromKotlinMarker::reason.name,
    HideFromKotlin::generation.name,
)

private val HIDE_FROM_JAVA_DATA = AnnotationData(
    HideFromJavaMarker::class.java.desc,
    PluginConfiguration.hideFromJava,
    HideFromJava::reason.name,
    HideFromJava::generation.name,
)

private val PACKAGE_PRIVATE_DATA = AnnotationData(
    PackagePrivateMarker::class.java.desc,
    PluginConfiguration.packagePrivate,
    PackagePrivate::reason.name,
    PackagePrivate::generation.name,
)
