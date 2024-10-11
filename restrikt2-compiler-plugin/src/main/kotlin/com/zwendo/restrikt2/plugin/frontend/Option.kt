package com.zwendo.restrikt2.plugin.frontend

import com.zwendo.restrikt2_compiler_plugin.BuildConfig
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.name.ClassId

/**
 * Convenience interface to represent a command line option.
 */
internal interface Option : AbstractCliOption {

    /**
     * Apply the value of the option to the compilation.
     */
    fun applyToCompilation(value: String)

    companion object {

        val OPTIONS: List<Option> = listOf(
            enabled,
            toplevelPrivateConstructor,
            automaticInternalHiding,
            annotationProcessing,
            hideFromJavaAnnotation,
            hideFromKotlinAnnotation,
            packagePrivateAnnotation,
            ignoreDefaultAnnotations,
        )

        val NAME_TO_OPTION = OPTIONS.associateBy { it.optionName }

    }

}

private class OptionImpl(
    override val optionName: String,
    override val valueDescription: String,
    override val description: String,
    override val allowMultipleOccurrences: Boolean = false,
    private val apply: (String) -> Unit,
) : Option {

    override val required: Boolean
        get() = false

    override fun applyToCompilation(value: String): Unit = apply(value)

}

private val enabled = OptionImpl(
    BuildConfig.ENABLED,
    "<true|false>",
    "Whether the plugin is enabled or not.",
) { PluginConfiguration.enabled = it.toBooleanStrict() }

private val toplevelPrivateConstructor = OptionImpl(
    BuildConfig.TOPLEVEL_PRIVATE_CONSTRUCTOR,
    "<true|false>",
    "Whether to generate private constructors for top-level classes.",
) { PluginConfiguration.toplevelPrivateConstructor = it.toBooleanStrict() }

private val automaticInternalHiding = OptionImpl(
    BuildConfig.AUTOMATIC_INTERNAL_HIDING,
    "<true|false>",
    "Whether internal symbols should be hidden automatically.",
) { PluginConfiguration.automaticInternalHiding = it.toBooleanStrict() }

private val annotationProcessing = OptionImpl(
    BuildConfig.ANNOTATION_PROCESSING,
    "<true|false>",
    "Whether the plugin should process annotations.",
) { PluginConfiguration.annotationProcessing = it.toBooleanStrict() }

private val ignoreDefaultAnnotations = OptionImpl(
    BuildConfig.IGNORE_DEFAULT_ANNOTATIONS,
    "<true|false>",
    "Whether to ignore the default when parsing the annotations (see documentation for more details)."
) {
    if (it.toBooleanStrict()) {
        PluginConfiguration.removeDefaultAnnotations()
    }
}

private val hideFromJavaAnnotation = OptionImpl(
    BuildConfig.HIDE_FROM_JAVA_ANNOTATION,
    "<annotation>",
    "Annotation marking symbols to be hidden from Java (package elements must be separated by a '/' and inner classes by a '.', e.g. name/of/the/package/OuterClass.MyAnnotation).",
    true,
) { PluginConfiguration.hideFromJavaAnnotations.add(ClassId.fromString(it)) }

private val hideFromKotlinAnnotation = OptionImpl(
    BuildConfig.HIDE_FROM_KOTLIN_ANNOTATION,
    "<annotation>",
    "Annotation marking symbols to be hidden from Kotlin (package elements must be separated by a '/' and inner classes by a '.', e.g. name/of/the/package/OuterClass.MyAnnotation).",
    true,
) { PluginConfiguration.hideFromKotlinAnnotations.add(ClassId.fromString(it)) }

private val packagePrivateAnnotation = OptionImpl(
    BuildConfig.PACKAGE_PRIVATE_ANNOTATION,
    "<annotation>",
    "Annotation marking symbols to have the package-private visibility (package elements must be separated by a '/' and inner classes by a '.', e.g. name/of/the/package/OuterClass.MyAnnotation).",
    true,
) { PluginConfiguration.packagePrivateAnnotations.add(ClassId.fromString(it)) }
