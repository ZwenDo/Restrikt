package com.zwendo.restrikt.plugin.frontend

import com.zwendo.restrikt_compiler_plugin.BuildConfig
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption

internal sealed interface Option : AbstractCliOption {

    override val required: Boolean
        get() = false

    override val allowMultipleOccurrences: Boolean
        get() = false

    fun applyToCompilation(input: String)

    companion object {

        val OPTIONS: List<Option> = listOf(
            enabled,
            toplevelPrivateConstructor,
            automaticInternalHiding,
            annotationProcessing,
            *annotationConfiguration(BuildConfig.HIDE_FROM_JAVA, PluginConfiguration.hideFromJava),
            *hideFromKotlinConfiguration(),
            *annotationConfiguration(BuildConfig.PACKAGE_PRIVATE, PluginConfiguration.packagePrivate),
        )

        val NAME_TO_OPTION = OPTIONS.associateBy { it.optionName }

    }

}

private class OptionImpl(
    override val optionName: String,
    override val valueDescription: String,
    override val description: String,
    private val apply: (String) -> Unit,
) : Option {

    override fun applyToCompilation(input: String) = apply(input)

}

private val enabled = OptionImpl(
    "enabled",
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

private val deprecatedReason = OptionImpl(
    "${BuildConfig.HIDE_FROM_KOTLIN}-${BuildConfig.ANNOTATION_POSTFIX_DEPRECATED_REASON}",
    "<reason>",
    "The reason for the generated Deprecated annotations.",
) { PluginConfiguration.hideFromKotlin.deprecatedReason = it }

private fun annotationEnabled(
    annotationName: String,
    annotationConfiguration: PluginConfiguration.AnnotationConfiguration
) = OptionImpl(
    "$annotationName-${BuildConfig.ANNOTATION_POSTFIX_ENABLED}",
    "<true|false>",
    "Whether the $annotationName annotation should be enabled.",
) { annotationConfiguration.enabled = it.toBooleanStrict() }

private fun annotationKeeping(
    annotationName: String,
    annotationConfiguration: PluginConfiguration.AnnotationConfiguration
) = OptionImpl(
    "$annotationName-${BuildConfig.ANNOTATION_POSTFIX_RETENTION}",
    "<source|binary|runtime>",
    "Whether the plugin should keep the $annotationName annotation.",
) { annotationConfiguration.retention = PluginConfiguration.AnnotationConfiguration.Retention.valueOf(it) }

private fun annotationDefaultReason(
    annotationName: String,
    annotationConfiguration: PluginConfiguration.AnnotationConfiguration
) = OptionImpl(
    "$annotationName-${BuildConfig.ANNOTATION_POSTFIX_DEFAULT_REASON}",
    "<reason>",
    "The default reason for the $annotationName annotation.",
) { annotationConfiguration.defaultReason = it }


private fun annotationConfiguration(
    annotationName: String,
    configuration: PluginConfiguration.AnnotationConfiguration,
) = arrayOf(
    annotationEnabled(annotationName, configuration),
    annotationKeeping(annotationName, configuration),
    annotationDefaultReason(annotationName, configuration),
)

private fun hideFromKotlinConfiguration() = arrayOf(
    deprecatedReason,
    *annotationConfiguration(BuildConfig.HIDE_FROM_KOTLIN, PluginConfiguration.hideFromKotlin),
)
